package mx.utng.ecoviedos.data.mqtt

object MqttConfig {
    const val BROKER_URL = "ssl://af91fb1b08fc4acca8986fd93abf0207.s1.eu.hivemq.cloud:8883"
    const val USERNAME = "EcoWear"
    const val PASSWORD = "ecowear#12"

    const val TOPIC_PARCELAS_LISTA = "vinedo/parcelas/lista"
    const val TOPIC_PARCELA_STATS = "vinedo/parcela/+/stats"
    const val TOPIC_RIEGO_CONTROL = "vinedo/parcela/%s/riego"
}
