package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

interface MuestraService {

    @POST("api/muestras")
    suspend fun registrarMuestra(
        @Header("Authorization") token: String,
        @Body request: MuestraRequest
    ): Response<MuestraResponse>

    @GET("api/muestras/parcela/{parcelaId}")
    suspend fun obtenerHistorialPorParcela(
        @Header("Authorization") token: String,
        @Path("parcelaId") parcelaId: String
    ): Response<List<MuestraResponse>>
}
