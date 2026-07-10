package mx.utng.ecoviedos.domain.model

import java.util.Date

data class Bitacora(
    val id: Int,
    val idParcela: String,
    val fecha: Date,
    var titulo: String,
    var descripcion: String,
    var audio: String?,
    var transcripcion: String?,
    var sincronizada: Boolean
)
