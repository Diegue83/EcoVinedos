package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BitacoraService {

    @GET("api/bitacoras")
    suspend fun obtenerBitacoras(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null
    ): Response<List<BitacoraResponse>>

    @GET("api/bitacoras/{id}")
    suspend fun obtenerBitacoraPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BitacoraResponse>

    @POST("api/bitacoras")
    suspend fun crearBitacora(
        @Header("Authorization") token: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    @PUT("api/bitacoras/{id}")
    suspend fun actualizarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    @DELETE("api/bitacoras/{id}")
    suspend fun eliminarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
