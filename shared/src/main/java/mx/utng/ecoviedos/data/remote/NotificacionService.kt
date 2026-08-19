package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para la gestión de notificaciones push y alertas.
 *
 * Permite consultar los avisos del sistema y actualizar su estado de lectura.
 */
interface NotificacionService {
    /**
     * Recupera todas las notificaciones dirigidas al usuario actual.
     *
     * @param token Token de sesión.
     * @return Lista de notificaciones.
     */
    @GET("api/notificaciones")
    suspend fun obtenerMisNotificaciones(
        @Header("Authorization") token: String
    ): Response<List<NotificacionResponse>>

    /**
     * Cambia el estado de una notificación (leída, archivada).
     *
     * @param token Token de sesión.
     * @param id ID de la notificación.
     * @param request Mapa con el nuevo estado (e.g. "estado" to "leida").
     * @return Notificación actualizada.
     */
    @PUT("api/notificaciones/{id}/estado")
    suspend fun cambiarEstado(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<NotificacionResponse>
}
