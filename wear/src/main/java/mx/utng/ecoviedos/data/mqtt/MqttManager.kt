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
    private val onSensorsUpdated: (parcelId: String, humedad: Float, temperatura: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val gson = Gson()
    private val clientId = "WearClient_${System.currentTimeMillis()}"

    fun connect() {
        try {
            mqttClient = MqttClient(MqttConfig.BROKER_URL, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 60
                
                if (MqttConfig.BROKER_URL.startsWith("ssl://")) {
                    sslProperties = java.util.Properties()
                }
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("MQTT_Wear", "Conectado a $serverURI")
                    mqttClient?.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                    mqttClient?.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT_Wear", "Conexión perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    try {
                        when {
                            topic == MqttConfig.TOPIC_PARCELAS_LISTA -> {
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
                                val parts = topic.split("/")
                                if (parts.size >= 3) {
                                    val parcelId = parts[2]
                                    val json = JSONObject(payload)
                                    val hum = json.optDouble("humedad", 0.0).toFloat()
                                    val temp = json.optDouble("temperatura", 0.0).toFloat()
                                    val riego = json.optBoolean("riegoActivo", false)
                                    val tiempo = json.optInt("tiempoRestante", 0)
                                    onSensorsUpdated(parcelId, hum, temp, riego, tiempo)
                                }
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

    fun activarRiego(idParcela: String, duracionMinutos: Int = 1) {
        try {
            val topic = String.format(MqttConfig.TOPIC_RIEGO_CONTROL, idParcela)
            val payload = JSONObject().apply {
                put("comando", "ON")
                put("duracion", duracionMinutos)
            }.toString()

            val mqttMessage = MqttMessage(payload.toByteArray()).apply { qos = 1 }
            mqttClient?.publish(topic, mqttMessage)
            Log.d("MQTT_Wear", "Riego activado parcela: $idParcela por $duracionMinutos min")
        } catch (e: Exception) {
            Log.e("MQTT_Wear", "Error activando riego: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {}
    }
}
