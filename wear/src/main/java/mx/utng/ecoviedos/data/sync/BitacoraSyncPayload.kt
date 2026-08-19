package mx.utng.ecoviedos.data.sync

/**
 * Estructura de datos utilizada para la serialización JSON durante la sincronización
 * mediante el Data Layer de Google.
 */
data class BitacoraSyncPayload(
    val id: Int,
    val idParcela: String,
    val fecha: Long, // epoch millis
    val titulo: String,
    val descripcion: String,
    val audio: String?
)
