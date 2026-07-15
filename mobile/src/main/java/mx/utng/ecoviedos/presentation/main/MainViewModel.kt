package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.utng.ecoviedos.data.WearableDataSender
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.remote.BitacoraRequest
import mx.utng.ecoviedos.data.remote.RiegoRequest
import mx.utng.ecoviedos.data.repository.BitacoraRemoteRepository
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.data.repository.RiegoRemoteRepository
import mx.utng.ecoviedos.data.sync.BitacoraSyncPayload
import mx.utng.ecoviedos.data.sync.RiegoSyncPayload
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val wearableDataSender = WearableDataSender(application)
    private val sessionManager = SessionManager(application)
    private val parcelaRepository = ParcelaRepository()
    private val bitacoraRemoteRepository = BitacoraRemoteRepository()
    private val riegoRemoteRepository = RiegoRemoteRepository()
    private val gson = Gson()
    private var mqttManager: MqttManager? = null

    private val currentParcelas = mutableListOf<Parcela>()
    private var currentMqttIp: String = "192.168.1.75" // valor por defecto hasta cargar el guardado

    init {
        Wearable.getMessageClient(application).addListener(this)

        mqttManager = MqttManager(application) { id, hum, temp ->
            viewModelScope.launch(Dispatchers.Main) {
                updateParcelaFromSensor(id, hum, temp)
            }
        }

        viewModelScope.launch {
            sessionManager.mqttIp.first()?.let { ipGuardada ->
                currentMqttIp = ipGuardada
            }
            cargarParcelas()
        }
    }

    // Usado por SettingsScreen para mostrar la IP actual
    fun getMqttIp(): String = currentMqttIp

    // Usado por SettingsScreen al guardar una nueva IP: persiste y reconecta
    fun updateMqttIp(nuevaIp: String) {
        currentMqttIp = nuevaIp
        viewModelScope.launch {
            sessionManager.guardarMqttIp(nuevaIp)
            launch(Dispatchers.IO) {
                mqttManager?.disconnect()
                mqttManager?.connect(currentMqttIp)
                currentParcelas.forEach { mqttManager?.subscribeToParcel(it.id) }
            }
        }
    }

    fun cargarParcelas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _error.value = "No hay sesión activa"
                _isLoading.value = false
                return@launch
            }

            val resultado = parcelaRepository.obtenerParcelas(token)
            resultado
                .onSuccess { parcelasDelBackend ->
                    currentParcelas.clear()
                    currentParcelas.addAll(parcelasDelBackend)
                    _parcelas.value = currentParcelas.toList()

                    launch(Dispatchers.IO) {
                        mqttManager?.connect(currentMqttIp)
                        currentParcelas.forEach { mqttManager?.subscribeToParcel(it.id) }
                    }

                    wearableDataSender.sendParcelas(currentParcelas.toList())
                }
                .onFailure { e ->
                    _error.value = "No se pudieron cargar las parcelas: ${e.message}"
                    Log.e("MainViewModel", "Error al cargar parcelas", e)
                }

            _isLoading.value = false
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            "/activate_irrigation" -> {
                val parcelId = String(event.data)
                updateParcelaFromSensor(parcelId, 45f, 24f)
            }
            "/sync_bitacora" -> sincronizarBitacorasDelReloj(event)
            "/sync_riego" -> sincronizarRiegosDelReloj(event)
        }
    }

    // Recibe las bitácoras pendientes del reloj y las sube al backend una por una
    private fun sincronizarBitacorasDelReloj(event: MessageEvent) {
        val json = String(event.data, Charsets.UTF_8)

        viewModelScope.launch {
            val idsConfirmados = mutableListOf<Int>()
            try {
                val itemType = object : TypeToken<List<BitacoraSyncPayload>>() {}.type
                val pendientes: List<BitacoraSyncPayload> = gson.fromJson(json, itemType)

                val token = sessionManager.token.first()
                if (token.isNullOrBlank()) {
                    Log.w("MainViewModel", "No hay sesión activa, no se puede sincronizar bitácoras")
                    return@launch
                }

                pendientes.forEach { item ->
                    val request = BitacoraRequest(
                        parcela = item.idParcela,
                        accion = item.titulo,
                        descripcion = item.descripcion
                    )
                    val resultado = bitacoraRemoteRepository.crearBitacora(token, request)
                    resultado.onSuccess { idsConfirmados.add(item.id) }
                        .onFailure { e ->
                            Log.e("MainViewModel", "No se pudo sincronizar bitácora ${item.id}", e)
                        }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error procesando bitácoras del reloj", e)
            }

            if (idsConfirmados.isNotEmpty()) {
                confirmarSincronizacionAlReloj(idsConfirmados)
            }
        }
    }

    private suspend fun confirmarSincronizacionAlReloj(idsConfirmados: List<Int>) {
        try {
            val nodes = Wearable.getNodeClient(getApplication<Application>()).connectedNodes.await()
            val json = gson.toJson(idsConfirmados)
            nodes.forEach { node ->
                Wearable.getMessageClient(getApplication<Application>())
                    .sendMessage(node.id, "/bitacora_synced", json.toByteArray(Charsets.UTF_8))
            }
            Log.d("MainViewModel", "Confirmadas ${idsConfirmados.size} bitácoras al reloj")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error confirmando sincronización al reloj", e)
        }
    }

    // Recibe los riegos pendientes del reloj y los sube al backend uno por uno
    private fun sincronizarRiegosDelReloj(event: MessageEvent) {
        val json = String(event.data, Charsets.UTF_8)

        viewModelScope.launch {
            val idsConfirmados = mutableListOf<Int>()
            try {
                val itemType = object : TypeToken<List<RiegoSyncPayload>>() {}.type
                val pendientes: List<RiegoSyncPayload> = gson.fromJson(json, itemType)

                val token = sessionManager.token.first()
                if (token.isNullOrBlank()) {
                    Log.w("MainViewModel", "No hay sesión activa, no se puede sincronizar riegos")
                    return@launch
                }

                pendientes.forEach { item ->
                    val request = RiegoRequest(
                        parcela = item.idParcela,
                        duracion = item.duracion.toDouble(),
                        litros = item.litros.toDouble(),
                        estado = item.estado
                    )
                    val resultado = riegoRemoteRepository.crearRiego(token, request)
                    resultado.onSuccess { idsConfirmados.add(item.id) }
                        .onFailure { e ->
                            Log.e("MainViewModel", "No se pudo sincronizar riego ${item.id}", e)
                        }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error procesando riegos del reloj", e)
            }

            if (idsConfirmados.isNotEmpty()) {
                confirmarSincronizacionRiegoAlReloj(idsConfirmados)
            }
        }
    }

    private suspend fun confirmarSincronizacionRiegoAlReloj(idsConfirmados: List<Int>) {
        try {
            val nodes = Wearable.getNodeClient(getApplication<Application>()).connectedNodes.await()
            val json = gson.toJson(idsConfirmados)
            nodes.forEach { node ->
                Wearable.getMessageClient(getApplication<Application>())
                    .sendMessage(node.id, "/riego_synced", json.toByteArray(Charsets.UTF_8))
            }
            Log.d("MainViewModel", "Confirmados ${idsConfirmados.size} riegos al reloj")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error confirmando sincronización de riego al reloj", e)
        }
    }

    private fun updateParcelaFromSensor(id: String, newHumidity: Float, newTemp: Float) {
        val index = currentParcelas.indexOfFirst { it.id == id }
        if (index != -1) {
            currentParcelas[index] = currentParcelas[index].copy(
                humedad = newHumidity,
                temperatura = newTemp
            )
            _parcelas.value = currentParcelas.toList()
            wearableDataSender.sendParcelas(currentParcelas.toList())
            Log.d("MQTT", "UI y Reloj actualizados para parcela $id")
        }
    }

    fun addNewParcel(nombre: String, variedad: String, area: Int, umbralH: Float, umbralT: Float) {
        val newId = (currentParcelas.size + 10).toString()
        val newParcel = Parcela(
            id = newId, nombreParcela = nombre, variedad = variedad, areaM2 = area,
            umbralHumedad = umbralH, umbralTemp = umbralT, indiceMaduracion = 0.5f,
            fechaCosecha = Date(), activa = true, humedad = 50f, temperatura = 24f
        )
        currentParcelas.add(newParcel)
        _parcelas.value = currentParcelas.toList()
        mqttManager?.subscribeToParcel(newId)
        wearableDataSender.sendParcelas(currentParcelas.toList())
    }

    fun reloadParcelas() {
        wearableDataSender.sendParcelas(_parcelas.value)
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
        Wearable.getMessageClient(getApplication()).removeListener(this)
    }
}