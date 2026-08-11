package mx.utng.ecoviedos.domain.model

import java.util.Date

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
    var humedad: Float = 0f,    // Campo real de sensor
    var temperatura: Float = 0f, // Campo real de sensor
    var humedadSuelo: Float = 0f, // Campo real de sensor
    var riegoActivo: Boolean = false, // Estado real del riego
    var tiempoRestanteRiego: Int = 0, // En segundos
    val brix: Float? = null,
    val ph: Float? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null,
    val consumoAguaM2: Float = 3.0f,
    val tipoRiego: String = "MANUAL", // "AUTO" o "MANUAL"
    val nodoVinculado: String? = null,
    var lastUpdated: Long = System.currentTimeMillis()
)


