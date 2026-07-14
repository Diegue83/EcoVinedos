package mx.utng.ecoviedos.data.mqtt

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.utng.ecoviedos.data.ParcelaMap
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.Date

class MqttManager(
    private val onSensorsUpdated: (parcelId: String, humedad: Float, temp: Float) -> Unit,
    private val onRiegoUpdate: (parcelId: String, estado: String) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val gson = Gson()
    private val serverUri = "tcp://10.0.2.2:1883" // IP por defecto, igual que en el móvil
    private val clientId = "WearClient_${System.currentTimeMillis()}"
    fun connect() {
        Log.d("MQTT_Wear", "Intentando conectar a $serverUri")
        try {
            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            Log.d("MQTT_Wear", "Intentando conectar a $serverUri")
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }
            Log.d("MQTT_Wear", "Intentando conectar a $serverUri")
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT_Wear", "Conexión perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    Log.d("MQTT_Wear", "Mensaje en $topic")

                    try {
                        when {
                            topic == "vinedo/parcelas/lista" -> {
                                // Recibir lista completa de parcelas
                                val itemType = object : TypeToken<List<ParcelaMap>>() {}.type
                                val parcelasMobile: List<ParcelaMap> = gson.fromJson(payload, itemType)
                                val parcelasWear = parcelasMobile.map { m ->
                                    Parcela(
                                        id = m.id,
                                        nombreParcela = m.nombreParcela ?: "Parcela ${m.id}",
                                        variedad = m.variedad ?: "",
                                        areaM2 = m.areaM2,
                                        umbralHumedad = m.umbralHumedad,
                                        umbralTemp = m.umbralTemp,
                                        indiceMaduracion = m.indiceMaduracion,
                                        fechaCosecha = m.fechaCosecha ?: Date(),
                                        activa = m.activa,
                                        humedad = m.humedad,
                                        temperatura = m.temperatura
                                    )
                                }
                                ParcelaRepository.updateParcelas(parcelasWear)
                            }
                            topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/stats") -> {
                                // Recibir datos de sensores individuales
                                val parts = topic.split("/")
                                if (parts.size >= 3) {
                                    val parcelId = parts[2]
                                    val json = JSONObject(payload)
                                    val hum = json.getDouble("humedad").toFloat()
                                    val temp = json.getDouble("temperatura").toFloat()
                                    onSensorsUpdated(parcelId, hum, temp)
                                }
                            }
                            topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/riego/switch") -> {
                                val parts = topic.split("/")
                                val parcelId = parts[2]
                                if (payload == "ON") {
                                    onRiegoUpdate(parcelId, "OFF")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT_Wear", "Error procesando mensaje: ${e}")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
            mqttClient?.subscribe("vinedo/parcelas/lista", 1)
            mqttClient?.subscribe("vinedo/parcela/+/stats", 1)
            Log.d("MQTT_Wear", "Conectado y suscrito")
        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Fallo conexión: ${e.message}")
        }
    }

    fun activarRiego(idParcela: String) {
        try {
            val topic = "vinedo/parcela/$idParcela/riego"
            val mensaje = "ON"

            val mqttMessage = MqttMessage(mensaje.toByteArray())
            mqttMessage.qos = 1

            mqttClient?.publish(topic, mqttMessage)

            Log.d("MQTT_Wear", "Riego activado parcela: $idParcela")

        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Error activando riego: ${e.message}")
        }
    }

    fun disconnect() {
        mqttClient?.disconnect()
    }
}
