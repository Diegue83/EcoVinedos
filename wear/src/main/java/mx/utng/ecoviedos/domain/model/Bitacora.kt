package mx.utng.ecoviedos.domain.model

import java.util.Date

/**
 * Representa una entrada en la bitácora de campo desde el reloj.
 *
 * @property id Identificador secuencial.
 * @property idParcela Parcela asociada.
 * @property fecha Momento de la creación.
 * @property titulo Título breve de la nota.
 * @property descripcion Contenido detallado.
 * @property audio Ruta al archivo de voz grabado.
 * @property transcripcion Texto convertido (opcional).
 * @property sincronizada Flag de envío al móvil.
 */
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
