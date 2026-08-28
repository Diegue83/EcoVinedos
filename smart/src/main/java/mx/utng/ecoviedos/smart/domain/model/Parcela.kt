package mx.utng.ecoviedos.smart.domain.model

data class Parcela(
    val id: String,
    val nombre: String,
    val variedad: String,
    val humedad: String,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
