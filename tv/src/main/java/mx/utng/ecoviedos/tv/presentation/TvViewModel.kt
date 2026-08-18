package mx.utng.ecoviedos.tv.presentation

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import mx.utng.ecoviedos.shared.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.remote.PairCodeRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.SeccionCavaResponse

sealed class TvUiState {
    data object Loading : TvUiState()
    data class NotLinked(val pairingCode: String) : TvUiState()
    data class Linked(val cavas: List<mx.utng.ecoviedos.data.remote.CavaResponse>) : TvUiState()
    data class Error(val message: String) : TvUiState()
}

class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvUiState>(TvUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var pairingJob: Job? = null
    private var mqttManager: MqttManager? = null

    private val deviceId: String = android.provider.Settings.Secure.getString(
        application.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    ) ?: "tv_emulator_id"

    init {
        startPairingProcess()
    }

    private fun startPairingProcess() {
        pairingJob?.cancel()
        pairingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.tvService.checkStatus(deviceId)
                    if (response.isSuccessful) {
                        val session = response.body()
                        if (session?.isLinked == true) {
                            cargarDatosCava()
                            break // Detener este bucle al estar vinculado
                        } else if (session != null) {
                            _uiState.value = TvUiState.NotLinked(session.pairingCode)
                        }
                    } else if (response.code() == 404) {
                        getNewPairingCode()
                    } else {
                        _uiState.value = TvUiState.Error("Servidor: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _uiState.value = TvUiState.Error("Error de conexión: ${e.localizedMessage}")
                }
                delay(5000)
            }
        }
    }

    private fun initializeMqtt() {
        mqttManager?.disconnect()
        mqttManager = MqttManager(
            context = getApplication(),
            onMessageReceived = { id, hum, temp, _, _, _ ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarSeccionEnTiempoReal(id, hum, temp)
                }
            },
            onRiegoStatusReceived = { _, _, _ -> },
            onParcelListReceived = { },
            onCavaListReceived = { payload ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarListaCavasMqtt(payload)
                }
            },
            onConnectionStatusChanged = { _, _ -> }
        )
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
        }
    }

    private fun actualizarSeccionEnTiempoReal(id: String, hum: Float, temp: Float) {
        val state = _uiState.value
        if (state is TvUiState.Linked) {
            var changed = false
            val updatedCavas = state.cavas.map { cava ->
                val index = cava.secciones.indexOfFirst { it._id == id }
                if (index != -1) {
                    changed = true
                    val updatedSecciones = cava.secciones.toMutableList()
                    updatedSecciones[index] = updatedSecciones[index].copy(
                        humedad = hum.toDouble(),
                        temperatura = temp.toDouble(),
                        ultimaLectura = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                    )
                    cava.copy(secciones = updatedSecciones)
                } else {
                    cava
                }
            }
            if (changed) {
                _uiState.value = TvUiState.Linked(updatedCavas)
            }
        }
    }

    private fun actualizarListaCavasMqtt(payload: String) {
        val state = _uiState.value
        if (state is TvUiState.Linked) {
            try {
                val type = object : TypeToken<List<SeccionCavaResponse>>() {}.type
                val list = Gson().fromJson<List<SeccionCavaResponse>>(payload, type)
                
                val updatedCavas = state.cavas.map { cava ->
                    val seccionesActualizadas = cava.secciones.map { seccion ->
                        list.find { it._id == seccion._id } ?: seccion
                    }
                    cava.copy(secciones = seccionesActualizadas)
                }
                _uiState.value = TvUiState.Linked(updatedCavas)
            } catch (e: Exception) {
                Log.e("TvViewModel", "Error parseando lista cavas MQTT", e)
            }
        }
    }

    private fun cargarDatosCava() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TvUiState.Linked(response.body()!!)
                    initializeMqtt()
                } else {
                    _uiState.value = TvUiState.Error("Error al cargar cavas: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TvUiState.Error("Error al cargar cavas: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun getNewPairingCode() {
        try {
            val response = RetrofitClient.tvService.getPairingCode(PairCodeRequest(deviceId))
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = TvUiState.NotLinked(response.body()!!.pairingCode)
            } else {
                _uiState.value = TvUiState.Error("Código: ${response.code()}")
            }
        } catch (e: Exception) {
            _uiState.value = TvUiState.Error("Error al obtener código: ${e.localizedMessage}")
        }
    }

    fun desvincularTv() {
        viewModelScope.launch {
            try {
                _uiState.value = TvUiState.Loading
                // Desconectar MQTT
                mqttManager?.disconnect()
                mqttManager = null
                // Avisar al servidor para romper el vínculo
                RetrofitClient.tvService.unlinkTV(PairCodeRequest(deviceId))
                // Reiniciar el proceso de emparejamiento
                startPairingProcess()
            } catch (e: Exception) {
                Log.e("TvViewModel", "Error al desvincular", e)
                startPairingProcess()
            }
        }
    }

    fun retry() {
        _uiState.value = TvUiState.Loading
        startPairingProcess()
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
    }
}
