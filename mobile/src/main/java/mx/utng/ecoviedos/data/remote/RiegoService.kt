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

interface RiegoService {

    @GET("api/riegos")
    suspend fun obtenerRiegos(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<RiegoResponse>>

    @GET("api/riegos/{id}")
    suspend fun obtenerRiegoPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<RiegoResponse>

    @POST("api/riegos")
    suspend fun crearRiego(
        @Header("Authorization") token: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    @PUT("api/riegos/{id}")
    suspend fun actualizarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    @DELETE("api/riegos/{id}")
    suspend fun eliminarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
