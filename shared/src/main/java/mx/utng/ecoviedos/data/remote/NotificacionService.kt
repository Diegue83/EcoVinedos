package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

interface NotificacionService {
    @GET("api/notificaciones")
    suspend fun obtenerMisNotificaciones(
        @Header("Authorization") token: String
    ): Response<List<NotificacionResponse>>

    @PUT("api/notificaciones/{id}/estado")
    suspend fun cambiarEstado(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<NotificacionResponse>
}
