package mx.utng.ecoviedos.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class MqttManager(
    context: Context,
    private val onMessageReceived: (parcelId: String, humedad: Float, temp: Float, humedadSuelo: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit,
    private val onParcelListReceived: (jsonPayload: String) -> Unit,
    private val onConnectionStatusChanged: (isConnected: Boolean, message: String?) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val clientId = "AndroidMobile_${System.currentTimeMillis()}"
    private var isConnecting = false

    fun connect(customBrokerUrl: String? = null) {
        if (isConnecting) return
        
        // Usar la URL personalizada si se proporciona y es válida, de lo contrario usar HiveMQ
        val serverUri = if (!customBrokerUrl.isNullOrBlank() && customBrokerUrl.startsWith("ssl://")) {
            customBrokerUrl
        } else {
            MqttConfig.BROKER_URL
        }
        
        try {
            isConnecting = true
            onConnectionStatusChanged(false, "Conectando a HiveMQ...")

            mqttClient?.let {
                try {
                    if (it.isConnected) it.disconnect()
                    it.close()
                } catch (e: Exception) { }
            }

            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 20
                keepAliveInterval = 60
                // HiveMQ Cloud requiere SSL/TLS
                sslProperties = java.util.Properties()
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    isConnecting = false
                    Log.d("MQTT", "Conectado a HiveMQ: $serverURI")
                    onConnectionStatusChanged(true, "Conectado")
                    subscribeToTopics()
                }

                override fun connectionLost(cause: Throwable?) {
                    isConnecting = false
                    Log.e("MQTT", "Conexión perdida: ${cause?.message}")
                    onConnectionStatusChanged(false, "Sin conexión")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return

                    try {
                        when {
                            topic == MqttConfig.TOPIC_PARCELAS_LISTA -> {
                                onParcelListReceived(payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true &&
                                    topic.endsWith("/stats") -> {

                                val parts = topic.split("/")
                                if (parts.size >= 3) {
                                    val parcelId = parts[2]
                                    val json = JSONObject(payload)
                                    // Datos del sensor
                                    val sensores = json.optJSONObject("sensores")
                                    val humedad = sensores?.optDouble("humedad_aire",0.0)?.toFloat() ?: 0f
                                    val temperatura = sensores?.optDouble(
                                        "temperatura_aire",
                                        0.0
                                    )?.toFloat() ?: 0f
                                    val humedadSuelo = sensores?.optDouble("humedad_suelo",0.0)?.toFloat() ?: 0f

                                    // Datos de riego enviados por ESP32/backend
                                    val riegoActivo = json.optBoolean(
                                        "riegoActivo",
                                        false
                                    )
                                    val tiempoRestante = json.optInt(
                                        "tiempoRestante",
                                        0
                                    )
                                    Log.d(
                                        "MQTT",
                                        """
                                        Parcela: $parcelId
                                        Humedad suelo: $humedad
                                        Temperatura: $temperatura
                                        Riego: $riegoActivo
                                        Tiempo: $tiempoRestante
                                        """.trimIndent()
                                    )

                                    onMessageReceived(
                                        parcelId,
                                        humedad,
                                        temperatura,
                                        humedadSuelo,
                                        riegoActivo,
                                        tiempoRestante
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(
                            "MQTT",
                            "Error procesando mensaje: ${e.message}"
                        )
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
            
        } catch (e: MqttException) {
            isConnecting = false
            val errorMsg = "Error HiveMQ: ${e.reasonCode}"
            Log.e("MQTT", "Fallo HiveMQ: $errorMsg", e)
            onConnectionStatusChanged(false, errorMsg)
        } catch (e: Exception) {
            isConnecting = false
            onConnectionStatusChanged(false, "Error de red")
        }
    }

    private fun subscribeToTopics() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                    it.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                }
            }
        } catch (e: Exception) { }
    }

    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int = 10) {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    val topic = String.format(MqttConfig.TOPIC_RIEGO_CONTROL, parcelId)
                    val payload = JSONObject().apply {
                        put("comando", if (activo) "ON" else "OFF")
                        put("duracion", duracionMinutos)
                    }.toString()
                    it.publish(topic, MqttMessage(payload.toByteArray()).apply { qos = 1 })
                }
            }
        } catch (e: Exception) { }
    }

    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                it.close()
            }
            mqttClient = null
            Log.d("MQTT", "Cliente desconectado y recursos liberados")
        } catch (e: Exception) { }
    }
}
