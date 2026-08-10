package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

interface NotificacionService {
    @GET("api/notificaciones")
    suspend fun obtenerNotificaciones(): Response<List<NotificacionResponse>>

    @PUT("api/notificaciones/{id}/leer")
    suspend fun marcarLeida(@Path("id") id: String): Response<NotificacionResponse>

    @DELETE("api/notificaciones/limpiar")
    suspend fun limpiarLeidas(): Response<Unit>
}
