package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

data class TvSessionResponse(
    val _id: String,
    val deviceId: String,
    val pairingCode: String,
    val isLinked: Boolean,
    val expiresAt: String
)

data class PairCodeRequest(
    val deviceId: String
)

data class LinkTvRequest(
    val pairingCode: String
)

interface TvService {
    @POST("api/tv/pair-code")
    suspend fun getPairingCode(@Body request: PairCodeRequest): Response<TvSessionResponse>

    @GET("api/tv/status/{deviceId}")
    suspend fun checkStatus(@Path("deviceId") deviceId: String): Response<TvSessionResponse>

    @POST("api/tv/link")
    suspend fun linkTV(
        @Header("Authorization") token: String,
        @Body request: LinkTvRequest
    ): Response<Map<String, String>>
}
