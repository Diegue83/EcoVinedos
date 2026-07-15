package mx.utng.ecoviedos.data.sync

data class RiegoSyncPayload(
    val id: Int,
    val idParcela: String,
    val fecha: Long,
    val duracion: Int,
    val litros: Float,
    val estado: String
)
