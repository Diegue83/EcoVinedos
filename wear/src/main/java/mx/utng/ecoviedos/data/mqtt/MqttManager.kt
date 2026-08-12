package mx.utng.ecoviedos.data.mqtt

import android.util.Log
import com.google.gson.Gson
import mx.utng.ecoviedos.data.ParcelaMap
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.Date

class MqttManager(
    private val onSensorsUpdated: (parcelId: String, humedad: Float, temperatura: Float, humedadSuelo: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit,
    private val onRiegoStatusReceived: (parcelId: String, activo: Boolean, tiempo: Int) -> Unit,
    private val onStatusChanged: (String) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val gson = Gson()
    private val clientId = "WearClient_${System.currentTimeMillis()}"

    fun connect() {
        if (mqttClient?.isConnected == true) {
            Log.d("MQTT_Wear", "Ya está conectado, omitiendo...")
            return
        }

        try {
            Log.d("MQTT_Wear", "Iniciando conexión a ${MqttConfig.BROKER_URL}...")
            onStatusChanged("Conectando al broker...")
            
            if (mqttClient == null) {
                mqttClient = MqttClient(MqttConfig.BROKER_URL, clientId, MemoryPersistence())
            }

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 15 // Reducido para Wear OS
                keepAliveInterval = 30 // Reducido para evitar desconexiones por NAT
                
                if (MqttConfig.BROKER_URL.startsWith("ssl://")) {
                    try {
                        val sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2")
                        sslContext.init(null, null, null)
                        socketFactory = sslContext.socketFactory
                    } catch (e: Exception) {
                        Log.e("MQTT_Wear", "Error configurando SSL context: ${e.message}")
                    }
                }
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("MQTT_Wear", "✅ CONECTADO EXITOSAMENTE a $serverURI (reconnect: $reconnect)")
                    onStatusChanged("Conectado")
                    try {
                        mqttClient?.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                        mqttClient?.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                        mqttClient?.subscribe("vinedo/parcela/+/riego", 1)
                        Log.d("MQTT_Wear", "Suscrito a tópicos")
                    } catch (e: Exception) {
                        Log.e("MQTT_Wear", "Error al suscribirse: ${e.message}")
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    val msg = cause?.message ?: "Desconocido"
                    Log.e("MQTT_Wear", "❌ Conexión perdida: $msg", cause)
                    onStatusChanged("Reconectando...")
                    
                    // Si el error es el aborto por software, a veces un cierre manual ayuda
                    if (msg.contains("Software caused connection abort")) {
                         Log.w("MQTT_Wear", "Detectado aborto por software, intentando limpiar...")
                         // Intentar reconectar manualmente después de un breve delay
                         android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                             connect()
                         }, 5000)
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    try {
                        when {
                            topic == MqttConfig.TOPIC_PARCELAS_LISTA -> {
                                handleParcelList(payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/stats") -> {
                                handleStats(topic, payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/riego") -> {
                                handleRiego(topic, payload)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT_Wear", "Error procesando mensaje: $e")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Fallo conexión: ${e.message}")
        }
    }

    private fun handleParcelList(payload: String) {
        try {
            val itemType = object : com.google.gson.reflect.TypeToken<List<ParcelaMap>>() {}.type
            val parcelasMobile: List<ParcelaMap> = gson.fromJson(payload, itemType)
            val parcelasWear = parcelasMobile.map { m ->
                Parcela(
                    id = m.id,
                    nombreParcela = m.nombreParcela ?: "Parcela ${m.id}",
                    variedad = m.variedad ?: "",
                    areaM2 = m.areaM2,
                    umbralHumedad = m.umbralHumedad,
                    umbralTemp = m.umbralTemp,
                    umbralHumedadSuelo = m.umbralHumedadSuelo ?: 40f,
                    indiceMaduracion = m.indiceMaduracion,
                    fechaCosecha = m.fechaCosecha ?: Date(),
                    activa = m.activa,
                    humedad = m.humedad,
                    temperatura = m.temperatura,
                    humedadSuelo = m.humedadSuelo ?: 0f
                )
            }
            ParcelaRepository.updateParcelas(parcelasWear)
        } catch (e: Exception) { Log.e("MQTT_Wear", "Error list: $e") }
    }

    private fun handleStats(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val hum = json.optDouble("humedad", 0.0).toFloat()
                val temp = json.optDouble("temperatura", 0.0).toFloat()
                val humSuelo = json.optDouble("humedadSuelo", 0.0).toFloat()
                val riego = json.optBoolean("riegoActivo", false)
                val tiempo = json.optInt("tiempoRestante", 0)
                onSensorsUpdated(parcelId, hum, temp, humSuelo, riego, tiempo)
            } catch (e: Exception) { Log.e("MQTT_Wear", "Error stats: $e") }
        }
    }

    private fun handleRiego(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val activo = json.optString("comando") == "ON" || json.optString("estado") == "ACTIVO"
                val duracionInput = json.optInt("duracion", 0)
                
                // Conversión de minutos a segundos para el cronómetro
                val tiempoSegundos = if (duracionInput > 0 && duracionInput < 120) duracionInput * 60 else duracionInput
                
                onRiegoStatusReceived(parcelId, activo, tiempoSegundos)
            } catch (e: Exception) { Log.e("MQTT_Wear", "Error riego: $e") }
        }
    }

    fun activarRiego(idParcela: String, comando: String = "ON", duracionMinutos: Int = 1) {
        try {
            val topic = String.format(MqttConfig.TOPIC_RIEGO_CONTROL, idParcela)
            val payload = JSONObject().apply {
                put("comando", comando)
                put("duracion", duracionMinutos)
            }.toString()

            val mqttMessage = MqttMessage(payload.toByteArray()).apply { qos = 1 }
            mqttClient?.publish(topic, mqttMessage)
            Log.d("MQTT_Wear", "Riego $comando parcela: $idParcela")
        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Error control riego: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                it.close()
            }
            mqttClient = null
        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Error al desconectar: ${e.message}")
        }
    }
}
