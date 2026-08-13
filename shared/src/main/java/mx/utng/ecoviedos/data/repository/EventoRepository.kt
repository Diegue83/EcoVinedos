package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

class EventoRepository {
    private val service = RetrofitClient.eventoService

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
