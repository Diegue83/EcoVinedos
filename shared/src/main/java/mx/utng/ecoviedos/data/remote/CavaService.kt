package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

data class CavaResponse(
    val _id: String,
    val nombre: String,
    val tipo: String,
    val temperatura: Double,
    val humedad: Double,
    val capacidadBotellas: Int,
    val botellasActuales: Int,
    val sensorId: String?,
    val estado: String
)

interface CavaService {
    @GET("api/cavas")
    suspend fun obtenerCavas(): Response<List<CavaResponse>>

    @PUT("api/cavas/{id}/sensor")
    suspend fun vincularSensor(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<CavaResponse>

    @PUT("api/cavas/{id}/botellas")
    suspend fun actualizarBotellas(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Int>
    ): Response<CavaResponse>
}
