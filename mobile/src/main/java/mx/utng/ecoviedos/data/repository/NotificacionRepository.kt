package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.NotificacionResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de notificaciones del sistema.
 */
class NotificacionRepository {

    /**
     * Obtiene todas las notificaciones (alertas de humedad, desconexión, etc.).
     * 
     * @return Resultado con la lista de notificaciones o error.
     */
    suspend fun obtenerNotificaciones(): Result<List<NotificacionResponse>> {
        return try {
            val response = RetrofitClient.notificacionService.obtenerNotificaciones()
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
     * Marca una notificación específica como leída.
     * 
     * @param id Identificador de la notificación.
     * @return Resultado unitario de éxito o error.
     */
    suspend fun marcarLeida(id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.notificacionService.marcarLeida(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al marcar como leída"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina todas las notificaciones que ya han sido leídas.
     * 
     * @return Resultado unitario de éxito o error.
     */
    suspend fun limpiarLeidas(): Result<Unit> {
        return try {
            val response = RetrofitClient.notificacionService.limpiarLeidas()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al limpiar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
