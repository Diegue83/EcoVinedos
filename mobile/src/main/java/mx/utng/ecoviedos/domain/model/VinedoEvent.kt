package mx.utng.ecoviedos.domain.model

import java.util.Date

data class VinedoEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    val imageUrl: String? = null,
    val type: String = "EVENT" // "EVENT", "TOURISM", "NOTICE"
)
