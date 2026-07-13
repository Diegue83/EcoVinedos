package mx.utng.ecoviedos.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class MqttManager(
    context: Context,
    private val onMessageReceived: (parcelId: String, humedad: Float, temp: Float) -> Unit
) {
    private var mqttClient: MqttClient? = null
    // IP de tu servidor Mosquitto (PC con Node-RED)
    private val serverUri = "tcp://192.168.1.75:1883" 
    private val clientId = "AndroidClient_${System.currentTimeMillis()}"

    fun connect() {
        try {
            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 10
            }

            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Conexión perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    Log.d("MQTT", "Mensaje recibido en $topic: $payload")
                    
                    try {
                        // Tópico esperado: vinedo/parcela/{id}/stats
                        val parts = topic?.split("/") ?: return
                        if (parts.size >= 3) {
                            val parcelId = parts[2]
                            val json = JSONObject(payload)
                            val hum = json.getDouble("humedad").toFloat()
                            val temp = json.getDouble("temperatura").toFloat()
                            onMessageReceived(parcelId, hum, temp)
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "Error parseando JSON: ${e.message}")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
            Log.d("MQTT", "Conectado exitosamente a $serverUri")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al conectar: ${e.message}")
        }
    }

    fun subscribeToParcel(parcelId: String) {
        val topic = "vinedo/parcela/$parcelId/stats"
        try {
            mqttClient?.subscribe(topic, 1)
            Log.d("MQTT", "Suscrito a: $topic")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al suscribir: ${e.message}")
        }
    }

    fun disconnect() {
        mqttClient?.disconnect()
    }
}
