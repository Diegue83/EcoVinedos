package mx.utng.ecoviedos.domain.model

import java.util.Date

/**
 * Modelo de dominio que representa una Parcela en el sistema EcoViñedos.
 *
 * Esta clase contiene tanto la configuración estática de la parcela (nombre, variedad, umbrales)
 * como el estado dinámico capturado por los sensores IoT en tiempo real (humedad, temperatura, riego).
 *
 * @property id Identificador único de la parcela.
 * @property nombreParcela Nombre descriptivo (e.g., "Sección Norte A").
 * @property variedad Tipo de uva cultivada (e.g., "Tempranillo").
 * @property areaM2 Superficie de la parcela en metros cuadrados.
 * @property umbralHumedad Límite crítico de humedad relativa para disparar alertas.
 * @property umbralTemp Límite crítico de temperatura ambiental.
 * @property umbralHumedadSuelo Porcentaje mínimo de humedad en suelo permitido.
 * @property humedadOptimaSuelo Valor objetivo de humedad en suelo tras el riego.
 * @property indiceMaduracion Valor calculado para determinar el punto óptimo de cosecha.
 * @property fechaCosecha Fecha estimada o programada para la vendimia.
 * @property activa Indica si la parcela está actualmente en producción y monitoreada.
 * @property humedad Valor actual de humedad ambiental (%) reportado por el sensor.
 * @property temperatura Valor actual de temperatura (°C) reportado por el sensor.
 * @property humedadSuelo Porcentaje actual de humedad en tierra reportado por el sensor.
 * @property riegoActivo Estado actual de la electroválvula de riego.
 * @property tiempoRestanteRiego Segundos pendientes para finalizar el ciclo de riego actual.
 * @property brix Grados Brix medidos en la última muestra (contenido de azúcar).
 * @property ph Nivel de acidez/alcalinidad del fruto.
 * @property acidez Porcentaje de acidez total.
 * @property phSuelo Nivel de pH de la tierra.
 * @property consumoAguaM2 Estimación de litros de agua necesarios por metro cuadrado.
 * @property tipoRiego Modo de operación: "AUTO" (basado en sensores) o "MANUAL" (activado por usuario).
 * @property nodoVinculado Identificador del hardware IoT (MAC o ID) vinculado a esta parcela.
 * @property lastUpdated Marca de tiempo (ms) de la última actualización de telemetría recibida.
 */
data class Parcela(
    val id: String,
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val umbralHumedadSuelo: Float,
    val humedadOptimaSuelo: Float,
    val indiceMaduracion: Float,
    val fechaCosecha: Date?,
    val activa: Boolean,
    var humedad: Float = 0f,
    var temperatura: Float = 0f,
    var humedadSuelo: Float = 0f,
    var riegoActivo: Boolean = false,
    var tiempoRestanteRiego: Int = 0,
    val brix: Float? = null,
    val ph: Float? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null,
    val consumoAguaM2: Float = 3.0f,
    val tipoRiego: String = "MANUAL",
    val nodoVinculado: String? = null,
    var lastUpdated: Long = System.currentTimeMillis()
)



