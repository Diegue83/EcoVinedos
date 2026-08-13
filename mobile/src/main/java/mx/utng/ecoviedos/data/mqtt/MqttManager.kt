package mx.utng.ecoviedos.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class MqttManager(
    context: Context,
    private val onMessageReceived: (parcelId: String, humedad: Float, temp: Float, humedadSuelo: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit,
    private val onRiegoStatusReceived: (parcelId: String, activo: Boolean, tiempo: Int) -> Unit,
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
            onConnectionStatusChanged(false, "Conectando al broker...")

            mqttClient?.let {
                try {
                    if (it.isConnected) it.disconnect()
                    it.close()
                } catch (e: Exception) { }
            }

            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                if (MqttConfig.USERNAME.isNotEmpty()) {
                    userName = MqttConfig.USERNAME
                    password = MqttConfig.PASSWORD.toCharArray()
                }
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 20
                keepAliveInterval = 60
                
                // Solo usar SSL si la URL lo indica
                if (serverUri.startsWith("ssl://")) {
                    sslProperties = java.util.Properties()
                }
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    isConnecting = false
                    Log.d("MQTT", "Conectado al broker: $serverURI")
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
                                // ... existing logic ...
                                handleStatsMessage(topic, payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true &&
                                    (topic.endsWith("/riego") || topic.endsWith("/control")) -> {
                                handleRiegoMessage(topic, payload)
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

    private fun handleStatsMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val sensores = json.optJSONObject("sensores")
                val hum = sensores?.optDouble("humedad_aire", 0.0)?.toFloat() ?: 0f
                val temp = sensores?.optDouble("temperatura_aire", 0.0)?.toFloat() ?: 0f
                val humSuelo = sensores?.optDouble("humedad_suelo", 0.0)?.toFloat() ?: 0f
                val riego = json.optBoolean("riegoActivo", false)
                val tiempo = json.optInt("tiempoRestante", 0)
                
                onMessageReceived(parcelId, hum, temp, humSuelo, riego, tiempo)
            } catch (e: Exception) {
                Log.e("MQTT", "Error parsing stats: ${e.message}")
            }
        }
    }

    private fun handleRiegoMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val comando = json.optString("comando", "")
                val estado = json.optString("estado", "")
                val activo = (comando == "ON" || estado == "ACTIVO")
                val duracionInput = json.optInt("duracion", 0)
                
                // Si el valor es pequeño (< 120), es muy probable que sean minutos.
                // Lo convertimos a segundos para el cronómetro de la app.
                val tiempoSegundos = if (duracionInput > 0 && duracionInput < 120) duracionInput * 60 else duracionInput
                
                onRiegoStatusReceived(parcelId, activo, tiempoSegundos)
            } catch (e: Exception) {
                Log.e("MQTT", "Error parsing riego: ${e.message}")
            }
        }
    }

    private fun subscribeToTopics() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                    it.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                    it.subscribe("vinedo/parcela/+/riego", 1)
                    it.subscribe("vinedo/parcela/+/control", 1)
                }
            }
        } catch (e: Exception) { }
    }

    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int = 1, modo: String = "AUTO") {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    val topic = String.format(MqttConfig.TOPIC_RIEGO_CONTROL, parcelId)
                    val payload = JSONObject().apply {
                        put("comando", if (activo) "ON" else "OFF")
                        put("duracion", duracionMinutos)
                        put("modo", modo) // "AUTO" or "MANUAL"
                    }.toString()
                    Log.d("MQTT", "Publicando a $topic: $payload")
                    it.publish(topic, MqttMessage(payload.toByteArray()).apply { qos = 1 })
                } else {
                    Log.w("MQTT", "No se pudo enviar comando: Cliente desconectado")
                }
            } ?: Log.w("MQTT", "No se pudo enviar comando: Cliente es null")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al publicar comando de riego", e)
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
        } catch (e: Exception) { }
    }
}
