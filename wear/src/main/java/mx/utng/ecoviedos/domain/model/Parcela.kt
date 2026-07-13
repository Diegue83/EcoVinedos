package mx.utng.ecoviedos.domain.model

import java.util.Date

data class Parcela(
    val id: String,
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val indiceMaduracion: Float,
    val fechaCosecha: Date,
    val activa: Boolean,
    var humedad: Float = 0f,    // Campo real de sensor
    var temperatura: Float = 0f, // Campo real de sensor
    var RIEGO_ACT: String = "OFF"
) {
    companion object {
        private const val HUMEDAD_CRITICA = 30f
    }

    // Verificar si la humedad es crítica
    fun esHumedadCritica(): Boolean {
        return humedad < HUMEDAD_CRITICA && RIEGO_ACT != "ON"
    }

    // Obtener estado de la parcela
    fun obtenerEstado(): String {
        return when {
            !activa -> "Inactiva"
            esHumedadCritica() -> "Humedad crítica"
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
