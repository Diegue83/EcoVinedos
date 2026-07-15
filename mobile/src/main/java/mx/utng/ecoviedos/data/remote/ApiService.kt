package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/parcelas")
    suspend fun obtenerParcelas(@Header("Authorization") token: String): Response<List<ParcelaResponse>>

    @POST("api/parcelas")
    suspend fun crearParcela(
        @Header("Authorization") token: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    // --- Bitácora ---
    @GET("api/bitacoras")
    suspend fun obtenerBitacoras(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null
    ): Response<List<BitacoraResponse>>

    @POST("api/bitacoras")
    suspend fun crearBitacora(
        @Header("Authorization") token: String,
        @Body bitacora: BitacoraRequest
    ): Response<BitacoraResponse>

    // --- Riego ---
    @GET("api/riegos")
    suspend fun obtenerRiegos(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<RiegoResponse>>

    @POST("api/riegos")
    suspend fun crearRiego(
        @Header("Authorization") token: String,
        @Body riego: RiegoRequest
    ): Response<RiegoResponse>

    @PUT("api/riegos/{id}")
    suspend fun actualizarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body riego: RiegoRequest
    ): Response<RiegoResponse>
}