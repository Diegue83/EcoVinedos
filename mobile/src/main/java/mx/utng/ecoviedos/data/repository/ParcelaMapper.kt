package mx.utng.ecoviedos.data.repository


import mx.utng.ecoviedos.data.remote.ParcelaResponse
import mx.utng.ecoviedos.domain.model.Parcela
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun parseFechaIso(fecha: String?): Date {
    if (fecha.isNullOrBlank()) return Date()
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        formato.parse(fecha) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

fun ParcelaResponse.toDomain(): Parcela {
    return Parcela(
        id = _id,
        nombreParcela = nombre,
        variedad = cultivo ?: "Sin especificar",
        areaM2 = superficie.toInt(),
        umbralHumedad = (umbralHumedad ?: 30.0).toFloat(),
        umbralTemp = (umbralTemp ?: 25.0).toFloat(),
        indiceMaduracion = (indiceMaduracion ?: 0.0).toFloat(),
        fechaCosecha = parseFechaIso(fechaCosecha),
        activa = estado == "activa",
        humedad = humedad.toFloat(),
        temperatura = temperatura.toFloat()
    )
}