package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ParcelaService {

    @GET("api/parcelas")
    suspend fun obtenerParcelas(@Header("Authorization") token: String): Response<List<ParcelaResponse>>

    @GET("api/parcelas/{id}")
    suspend fun obtenerParcelaPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ParcelaResponse>

    @POST("api/parcelas")
    suspend fun crearParcela(
        @Header("Authorization") token: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    @PUT("api/parcelas/{id}")
    suspend fun actualizarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    @DELETE("api/parcelas/{id}")
    suspend fun eliminarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
