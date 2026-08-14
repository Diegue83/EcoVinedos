package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.NotificacionResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de notificaciones del sistema.
 */
class NotificacionRepository {

    /**
     * Obtiene las notificaciones del usuario autenticado.
     */
    suspend fun obtenerMisNotificaciones(token: String): Result<List<NotificacionResponse>> {
        return try {
            val response = RetrofitClient.notificacionService.obtenerMisNotificaciones("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cambia el estado de una notificación (leida, no leida, descartada).
     */
    suspend fun cambiarEstado(token: String, id: String, estado: String): Result<NotificacionResponse> {
        return try {
            val response = RetrofitClient.notificacionService.cambiarEstado(
                "Bearer $token", 
                id, 
                mapOf("estado" to estado)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cambiar estado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
