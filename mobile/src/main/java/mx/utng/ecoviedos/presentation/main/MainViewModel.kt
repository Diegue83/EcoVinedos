package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.WearableDataSender
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.repository.ParcelaRepository
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
    private var mqttManager: MqttManager? = null

    // Ahora empieza vacía; se llena con datos reales del backend en cargarParcelas()
    private val currentParcelas = mutableListOf<Parcela>()

    init {
        Wearable.getMessageClient(application).addListener(this)

        mqttManager = MqttManager(application) { id, hum, temp ->
            viewModelScope.launch(Dispatchers.Main) {
                updateParcelaFromSensor(id, hum, temp)
            }
        }

        cargarParcelas()
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

                    // Conectar MQTT y suscribir cada parcela ya con datos reales
                    launch(Dispatchers.IO) {
                        mqttManager?.connect()
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
        if (event.path == "/activate_irrigation") {
            val parcelId = String(event.data)
            updateParcelaFromSensor(parcelId, 45f, 24f)
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

    // TODO: conectar con ParcelaRepository.crearParcela() para persistir en el backend
    // en vez de solo agregar localmente. Por ahora se mantiene local hasta implementar
    // el flujo de creación desde AddParcelScreen.
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