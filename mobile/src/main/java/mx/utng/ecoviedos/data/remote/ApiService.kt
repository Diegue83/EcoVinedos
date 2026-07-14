package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
}