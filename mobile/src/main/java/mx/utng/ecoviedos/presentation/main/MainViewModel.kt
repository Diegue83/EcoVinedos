package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    
    private val wearableDataSender = WearableDataSender(application)
    private var mqttManager: MqttManager? = null

    private val currentParcelas = mutableListOf(
        Parcela("4", "Merlot", "Variedad 1", 1000, 30f, 25f, 0.74f, Date(), true, 42f, 22f),
        Parcela("7", "Cabernet", "Variedad 2", 1500, 30f, 25f, 0.65f, Date(), true, 22f, 26f),
        Parcela("9", "Syrah", "Variedad 3", 1200, 30f, 25f, 0.68f, Date(), true, 65f, 20f)
    )

    init {
        _parcelas.value = currentParcelas.toList()
        Wearable.getMessageClient(application).addListener(this)
        
        // Inicializar MQTT
        mqttManager = MqttManager(application) { id, hum, temp ->
            viewModelScope.launch(Dispatchers.Main) {
                updateParcelaFromSensor(id, hum, temp)
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
            // Suscribir cada parcela individualmente a su canal
            currentParcelas.forEach { mqttManager?.subscribeToParcel(it.id) }
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
            // Sincronizar automáticamente con el reloj
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
        mqttManager?.subscribeToParcel(newId) // Suscribir la nueva parcela al canal MQTT
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
