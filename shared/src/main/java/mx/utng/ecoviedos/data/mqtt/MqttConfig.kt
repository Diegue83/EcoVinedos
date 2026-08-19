package mx.utng.ecoviedos.shared.data.mqtt

/**
 * Configuración centralizada para la comunicación MQTT.
 *
 * Contiene la URL del broker, credenciales de autenticación y los tópicos
 * utilizados para la telemetría y control de los dispositivos IoT.
 */
object MqttConfig {
    /** URL del broker HiveMQ con soporte SSL. */
    const val BROKER_URL = "ssl://af91fb1b08fc4acca8986fd93abf0207.s1.eu.hivemq.cloud:8883"
    /** Usuario para la conexión al broker. */
    const val USERNAME = "EcoVinMobile"
    /** Contraseña para la conexión al broker. */
    const val PASSWORD = "ecovin$12#34"
    
    /** Tópico para recibir la lista actualizada de parcelas. */
    const val TOPIC_PARCELAS_LISTA = "vinedo/parcelas/lista"
    /** Tópico para recibir la lista actualizada de secciones de cava. */
    const val TOPIC_SECCIONES_LISTA = "vinedo/secciones/lista"
    /** Patrón de tópico para recibir estadísticas de sensores (+ es comodín para ID de parcela). */
    const val TOPIC_PARCELA_STATS = "vinedo/parcela/+/stats"
    /** Formato de tópico para enviar comandos de control de riego. */
    const val TOPIC_RIEGO_CONTROL = "vinedo/parcela/%s/control"
}
