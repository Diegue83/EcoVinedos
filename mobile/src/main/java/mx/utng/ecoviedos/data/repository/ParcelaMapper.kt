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
        nombreParcela = nombreParcela ?: "Parcela sin nombre",
        variedad = variedad ?: "Sin especificar",
        areaM2 = (areaM2 ?: 0.0).toInt(),
        umbralHumedad = (umbralHumedad ?: 30.0).toFloat(),
        umbralTemp = (umbralTemp ?: 25.0).toFloat(),
        umbralHumedadSuelo = (umbralHumedadSuelo ?: 40.0).toFloat(),
        humedadOptimaSuelo = (humedadOptimaSuelo ?: 70.0).toFloat(),
        indiceMaduracion = (indiceMaduracion ?: 0.0).toFloat(),
        fechaCosecha = parseFechaIso(fechaCosecha),
        activa = activa ?: true,
        humedad = (humedad ?: 0.0).toFloat(),
        temperatura = (temperatura ?: 0.0).toFloat(),
        humedadSuelo = (humedadSuelo ?: 0.0).toFloat(),
        riegoActivo = riegoActivo ?: false,
        tiempoRestanteRiego = (tiempoRestanteRiego ?: 0) * 60, // Servidor guarda minutos, app usa segundos
        brix = brix?.toFloat(),
        ph = ph?.toFloat(),
        acidez = acidez?.toFloat(),
        phSuelo = phSuelo?.toFloat(),
        consumoAguaM2 = (consumoAguaM2 ?: 3.0).toFloat(),
        tipoRiego = (tipoRiego ?: "MANUAL").uppercase(),
        nodoVinculado = nodoVinculado
    )
}
