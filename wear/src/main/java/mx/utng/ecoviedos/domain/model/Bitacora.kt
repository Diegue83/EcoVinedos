package mx.utng.ecoviedos.domain.model

import java.util.Date

class Bitacora(
    var id: Int,
    var idParcela: Int,
    var fecha: Date,
    var titulo: String,
    var descripcion: String,
    var audio: String?,
    var transcripcion: String?,
    var sincronizada: Boolean
) {

    // Actualizar título
    fun actualizarTitulo(nuevoTitulo: String) {
        titulo = nuevoTitulo
    }

    // Actualizar descripción
    fun actualizarDescripcion(nuevaDescripcion: String) {
        descripcion = nuevaDescripcion
    }

    // Agregar o cambiar el audio
    fun actualizarAudio(nuevoAudio: String) {
        audio = nuevoAudio
    }

    // Agregar o cambiar la transcripción
    fun actualizarTranscripcion(nuevaTranscripcion: String) {
        transcripcion = nuevaTranscripcion
    }

    // Cambiar la fecha
    fun actualizarFecha(nuevaFecha: Date) {
        fecha = nuevaFecha
    }

    // Marcar como sincronizada
    fun sincronizar() {
        sincronizada = true
    }

    // Marcar como pendiente de sincronización
    fun desincronizar() {
        sincronizada = false
    }

    // Verificar si tiene audio
    fun tieneAudio(): Boolean {
        return !audio.isNullOrBlank()
    }

    // Verificar si tiene transcripción
    fun tieneTranscripcion(): Boolean {
        return !transcripcion.isNullOrBlank()
    }

    override fun toString(): String {
        return """
            Bitacora(
                id=$id,
                idParcela=$idParcela,
                fecha=$fecha,
                titulo='$titulo',
                descripcion='$descripcion',
                audio=${tieneAudio()},
                transcripcion=${tieneTranscripcion()},
                sincronizada=$sincronizada
            )
        """.trimIndent()
    }
}