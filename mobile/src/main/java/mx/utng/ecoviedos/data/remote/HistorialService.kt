package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class HistorialSensorResponse(
    val _id: String,
    val parcela: String,
    val humedadAire: Double,
    val temperaturaAire: Double,
    val humedadSuelo: Double,
    val fecha: String
)

data class ResumenDiarioResponse(
    val _id: String,
    val parcela: String,
    val humedadAirePromedio: Double,
    val temperaturaAirePromedio: Double,
    val humedadSueloPromedio: Double,
    val fecha: String
)

interface HistorialService {
    @GET("api/historial/parcela/{parcelaId}")
    suspend fun obtenerHistorialParcela(
        @Path("parcelaId") parcelaId: String,
        @Query("limit") limit: Int = 100
    ): Response<List<HistorialSensorResponse>>

    @GET("api/historial/resumen/{parcelaId}")
    suspend fun obtenerResumenParcela(
        @Path("parcelaId") parcelaId: String
    ): Response<List<ResumenDiarioResponse>>
}
