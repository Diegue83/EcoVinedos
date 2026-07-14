package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.content.Context
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
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.WearableDataSender
import mx.utng.ecoviedos.data.mqtt.MqttManager
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas.asStateFlow()

    private val _mqttStatus = MutableStateFlow("Desconectado")
    val mqttStatus: StateFlow<String> = _mqttStatus.asStateFlow()

    private val _isMqttConnected = MutableStateFlow(false)
    val isMqttConnected: StateFlow<Boolean> = _isMqttConnected.asStateFlow()
    
    private val wearableDataSender = WearableDataSender(application)
    private var mqttManager: MqttManager? = null
    private val gson = Gson()

    private val prefs = application.getSharedPreferences("EcoViñedosPrefs", Context.MODE_PRIVATE)

    init {
        Wearable.getMessageClient(application).addListener(this)
        val savedIp = prefs.getString("mqtt_server_ip", "192.168.1.75") ?: "192.168.1.75"
        initializeMqtt(savedIp)
    }

    private fun initializeMqtt(serverIp: String) {
        mqttManager?.disconnect()
        
        mqttManager = MqttManager(
            context = getApplication(),
            onMessageReceived = { id, hum, temp, riego, tiempo ->
                viewModelScope.launch(Dispatchers.Main) {
                    updateParcelaFromSensor(id, hum, temp, riego, tiempo)
                }
            },
            onParcelListReceived = { json ->
                viewModelScope.launch(Dispatchers.Main) {
                    updateFullParcelList(json)
                }
            },
            onConnectionStatusChanged = { connected, message ->
                viewModelScope.launch(Dispatchers.Main) {
                    _isMqttConnected.value = connected
                    _mqttStatus.value = message ?: if (connected) "Conectado" else "Desconectado"
                }
            }
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect(serverIp)
        }
    }

    private fun updateFullParcelList(json: String) {
        try {
            val itemType = object : TypeToken<List<Parcela>>() {}.type
            val newList: List<Parcela> = gson.fromJson(json, itemType)
            _parcelas.value = newList
            // Sincronizar con el reloj también
            wearableDataSender.sendParcelas(newList)
            Log.d("MQTT", "Lista completa de parcelas actualizada")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al procesar lista de parcelas", e)
        }
    }

    private fun updateParcelaFromSensor(id: String, hum: Float, temp: Float, riego: Boolean, tiempo: Int) {
        val currentList = _parcelas.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                humedad = hum,
                temperatura = temp,
                riegoActivo = riego,
                tiempoRestanteRiego = tiempo
            )
            _parcelas.value = currentList.toList()
            // Sincronizar con el reloj
            wearableDataSender.sendParcelas(currentList.toList())
        }
    }

    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int) {
        mqttManager?.toggleRiego(parcelId, activo, duracionMinutos)
    }

    fun updateMqttIp(newIp: String) {
        prefs.edit().putString("mqtt_server_ip", newIp).apply()
        initializeMqtt(newIp)
    }

    fun getMqttIp(): String {
        return prefs.getString("mqtt_server_ip", "192.168.1.75") ?: "192.168.1.75"
    }

    fun reloadParcelas() {
        wearableDataSender.sendParcelas(_parcelas.value)
    }

    override fun onMessageReceived(event: MessageEvent) {
        // Manejar activación de riego desde el reloj si es necesario
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
        Wearable.getMessageClient(getApplication()).removeListener(this)
    }
}
