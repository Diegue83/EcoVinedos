package mx.utng.ecoviedos.domain.model

import java.util.Date

data class VinedoEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imageUrl: String? = null,
    val type: String = "EVENT" // "EVENT", "TOURISM"
)
