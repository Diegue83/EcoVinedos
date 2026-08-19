package mx.utng.ecoviedos.domain.model

import java.util.Date

/**
 * Modelo de dominio que representa un Evento o Actividad turística.
 *
 * @property id Identificador único.
 * @property title Nombre del evento.
 * @property description Detalles del evento.
 * @property date Fecha programada.
 * @property precio Costo por persona.
 * @property cupo Límite de asistentes.
 * @property imageUrl URL de la imagen promocional.
 * @property type Categoría del evento (EVENT o TOURISM).
 */
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
