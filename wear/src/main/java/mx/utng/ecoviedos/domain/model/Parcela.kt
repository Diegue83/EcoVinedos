package mx.utng.ecoviedos.domain.model

import java.util.Date

data class Parcela(
    val id: String,
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val umbralHumedadSuelo: Float = 40f,
    val indiceMaduracion: Float,
    val fechaCosecha: Date,
    val activa: Boolean,
    var humedad: Float = 0f,    // Campo real de sensor
    var temperatura: Float = 0f, // Campo real de sensor
    var humedadSuelo: Float = 0f, // Campo real de sensor
    var riegoActivo: Boolean = false,
    var tiempoRestanteRiego: Int = 0,
    val tipoRiego: String = "MANUAL",
    val nodoVinculado: String? = null
) {
    // Verificar si la humedad es crítica (basado en humedad del suelo)
    fun esHumedadCritica(): Boolean {
        return humedadSuelo < umbralHumedadSuelo && !riegoActivo
    }

    // Obtener estado de la parcela
    fun obtenerEstado(): String {
        return when {
            !activa -> "Inactiva"
            esHumedadCritica() -> "Humedad suelo crítica"
            else -> "Estado normal"
        }
    }

    // Verificar si está próxima la cosecha (7 días o menos)
    fun estaProximaLaCosecha(): Boolean {
        val diasRestantes =
            (fechaCosecha.time - Date().time) / (1000 * 60 * 60 * 24)
        return diasRestantes <= 7
    }
}
