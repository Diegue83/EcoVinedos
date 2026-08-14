package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de eventos de turismo y actividades en el viñedo.
 *
 * Facilita el acceso a los datos de eventos a través de la API REST.
 */
class EventoRepository {
    private val service = RetrofitClient.eventoService

    /**
     * Obtiene una lista de eventos, opcionalmente filtrada por tipo.
     *
     * @param tipo Categoría del evento (opcional).
     * @return [Result] con la lista de [EventoResponse].
     */
    suspend fun obtenerEventos(tipo: String? = null): Result<List<EventoResponse>> {
        return try {
            val response = service.obtenerEventos(tipo)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener eventos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo evento en el sistema.
     *
     * @param token Token de autenticación.
     * @param request Datos del evento a crear.
     * @return [Result] con el [EventoResponse] creado.
     */
    suspend fun crearEvento(token: String, request: EventoRequest): Result<EventoResponse> {
        return try {
            val response = service.crearEvento("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear evento"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un evento existente.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del evento.
     * @return [Result] con éxito booleano.
     */
    suspend fun eliminarEvento(token: String, id: String): Result<Boolean> {
        return try {
            val response = service.eliminarEvento("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

