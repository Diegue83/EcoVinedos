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
    val humedad: Float = 0f,    // Campo real de sensor
    val temperatura: Float = 0f, // Campo real de sensor
    val brix: Int? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null
)


