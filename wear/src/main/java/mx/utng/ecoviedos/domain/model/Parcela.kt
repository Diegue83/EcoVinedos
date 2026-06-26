package mx.utng.ecovinedos.domain.model

import java.util.Date

class Parcela(
    var id: Int,
    var nombre: String,
    var variedad: String,
    var areaM2: Int,
    var humedad: Float,
    var indiceMaduracion: Float,
    var temperatura: Float,
    var fechaCosecha: Date,
    var activa: Boolean
) {

    companion object {
        private const val HUMEDAD_CRITICA = 30f
    }

    // Actualizar humedad
    fun actualizarHumedad(nuevaHumedad: Float) {
        humedad = nuevaHumedad
    }

    // Actualizar temperatura
    fun actualizarTemperatura(nuevaTemperatura: Float) {
        temperatura = nuevaTemperatura
    }

    // Actualizar índice de maduración
    fun actualizarIndiceMaduracion(nuevoIndice: Float) {
        indiceMaduracion = nuevoIndice
    }

    // Cambiar fecha de cosecha
    fun actualizarFechaCosecha(nuevaFecha: Date) {
        fechaCosecha = nuevaFecha
    }

    // Cambiar nombre
    fun actualizarNombre(nuevoNombre: String) {
        nombre = nuevoNombre
    }

    // Cambiar variedad
    fun actualizarVariedad(nuevaVariedad: String) {
        variedad = nuevaVariedad
    }

    // Cambiar área
    fun actualizarArea(nuevaArea: Int) {
        areaM2 = nuevaArea
    }

    // Activar parcela
    fun activar() {
        activa = true
    }

    // Desactivar parcela
    fun desactivar() {
        activa = false
    }

    // Verificar si la humedad es crítica
    fun esHumedadCritica(): Boolean {
        return humedad < HUMEDAD_CRITICA
    }

    // Obtener estado de la parcela
    fun obtenerEstado(): String {
        return when {
            !activa -> "Inactiva"
            esHumedadCritica() -> "Humedad crítica"
            else -> "Estado normal"
        }
    }

    // Verificar si está próxima la cosecha (7 días o menos)
    fun estaProximaLaCosecha(): Boolean {
        val diasRestantes =
            (fechaCosecha.time - Date().time) / (1000 * 60 * 60 * 24)
        return diasRestantes <= 7
    }

    override fun toString(): String {
        return """
            Parcela(
                id=$id,
                nombre='$nombre',
                variedad='$variedad',
                área=$areaM2 m²,
                humedad=$humedad%,
                temperatura=$temperatura°C,
                índiceMaduración=$indiceMaduracion,
                fechaCosecha=$fechaCosecha,
                activa=$activa,
                estado=${obtenerEstado()}
            )
        """.trimIndent()
    }
}