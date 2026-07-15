package mx.utng.ecoviedos.data.sync

data class BitacoraSyncPayload(
    val id: Int,
    val idParcela: String,
    val fecha: Long, // epoch millis
    val titulo: String,
    val descripcion: String,
    val audio: String?
)