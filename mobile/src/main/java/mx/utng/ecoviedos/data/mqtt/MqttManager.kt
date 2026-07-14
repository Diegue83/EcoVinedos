package mx.utng.ecoviedos.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class MqttManager(
    context: Context,
    private val onMessageReceived: (parcelId: String, humedad: Float, temp: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit,
    private val onParcelListReceived: (jsonPayload: String) -> Unit,
    private val onConnectionStatusChanged: (isConnected: Boolean, message: String?) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val clientId = "AndroidMobile_${System.currentTimeMillis()}"
    private var isConnecting = false

    fun connect(serverIp: String) {
        if (isConnecting) {
            Log.d("MQTT", "Ya hay un intento de conexión en curso...")
            return
        }
        
        // Limpiar IP de espacios en blanco
        val trimmedIp = serverIp.trim()
        val serverUri = "tcp://$trimmedIp:1883"
        
        try {
            isConnecting = true
            onConnectionStatusChanged(false, "Intentando conectar...")

            // Cerrar cliente anterior de forma segura
            mqttClient?.let {
                try {
                    if (it.isConnected) it.disconnect()
                    it.close()
                } catch (e: Exception) {
                    Log.w("MQTT", "Aviso al cerrar cliente previo: ${e.message}")
                }
            }

            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 15 // Tiempo de espera razonable
                keepAliveInterval = 60
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    isConnecting = false
                    Log.d("MQTT", "Conexión completada a $serverURI (reconnect: $reconnect)")
                    onConnectionStatusChanged(true, "Conectado a $serverURI")
                    if (reconnect) {
                        subscribeToTopics()
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    isConnecting = false
                    Log.e("MQTT", "Conexión perdida: ${cause?.message}")
                    onConnectionStatusChanged(false, "Conexión perdida")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    Log.d("MQTT", "Mensaje en [$topic]: $payload")
                    
                    try {
                        when {
                            topic == "vinedo/parcelas/lista" -> {
                                onParcelListReceived(payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/stats") -> {
                                val parts = topic.split("/")
                                if (parts.size >= 3) {
                                    val parcelId = parts[2]
                                    val json = JSONObject(payload)
                                    val hum = json.optDouble("humedad", 0.0).toFloat()
                                    val temp = json.optDouble("temperatura", 0.0).toFloat()
                                    val riego = json.optBoolean("riegoActivo", false)
                                    val tiempo = json.optInt("tiempoRestante", 0)
                                    onMessageReceived(parcelId, hum, temp, riego, tiempo)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "Error parseando mensaje en $topic", e)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            Log.d("MQTT", "Intentando conectar a $serverUri...")
            mqttClient?.connect(options)
            
            // Suscripción inicial
            subscribeToTopics()
            Log.d("MQTT", "Móvil conectado y suscrito a $serverUri")
            
        } catch (e: MqttException) {
            isConnecting = false
            val errorMsg = when (e.reasonCode.toInt()) {
                MqttException.REASON_CODE_SERVER_CONNECT_ERROR.toInt() -> 
                    "Servidor no disponible"
                32100 -> "Conexión ya en curso"
                0 -> "Tiempo de espera agotado"
                else -> "Error: ${e.reasonCode}"
            }
            Log.e("MQTT", "Fallo al conectar a $serverUri: $errorMsg", e)
            onConnectionStatusChanged(false, errorMsg)
        } catch (e: Exception) {
            isConnecting = false
            Log.e("MQTT", "Error inesperado al conectar a $serverUri", e)
            onConnectionStatusChanged(false, "Error de conexión")
        }
    }

    private fun subscribeToTopics() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.subscribe("vinedo/parcelas/lista", 1)
                    it.subscribe("vinedo/parcela/+/stats", 1)
                    Log.d("MQTT", "Suscrito a tópicos exitosamente")
                }
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Error al suscribirse", e)
        }
    }

    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int = 10) {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    val topic = "vinedo/parcela/$parcelId/riego/control"
                    val payload = JSONObject().apply {
                        put("comando", if (activo) "ON" else "OFF")
                        put("duracion", duracionMinutos)
                    }.toString()
                    val message = MqttMessage(payload.toByteArray()).apply { qos = 1 }
                    it.publish(topic, message)
                    Log.d("MQTT", "Comando de riego enviado a $topic: $payload")
                }
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Error al enviar comando de riego", e)
        }
    }

    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                it.close()
            }
            mqttClient = null
            Log.d("MQTT", "Cliente desconectado y recursos liberados")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al desconectar: ${e.message}")
        }
    }
}
