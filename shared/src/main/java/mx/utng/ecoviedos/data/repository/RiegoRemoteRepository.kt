package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.RiegoRequest
import mx.utng.ecoviedos.data.remote.RiegoResponse

/**
 * Repositorio para la gestión remota de eventos de riego.
 *
 * Provee métodos para consultar el historial de riegos realizados y registrar
 * nuevas acciones de riego en el servidor.
 */
class RiegoRemoteRepository {

    /**
     * Obtiene el listado de riegos registrados.
     *
     * @param token Token de autenticación JWT.
     * @param parcelaId Filtro opcional por parcela.
     * @param estado Filtro opcional por estado (e.g., "finalizado").
     * @return [Result] con la lista de riegos encontrados.
     */
    suspend fun obtenerRiegos(
        token: String,
        parcelaId: String? = null,
        estado: String? = null
    ): Result<List<RiegoResponse>> {
        return try {
            val response = RetrofitClient.riegoService.obtenerRiegos("Bearer $token", parcelaId, estado)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo evento de riego en el sistema.
     *
     * @param token Token de autenticación.
     * @param request Datos del riego a registrar.
     * @return [Result] con la confirmación del riego creado.
     */
    suspend fun crearRiego(token: String, request: RiegoRequest): Result<RiegoResponse> {
        return try {
            val response = RetrofitClient.riegoService.crearRiego("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un registro de riego existente.
     */
    suspend fun actualizarRiego(token: String, id: String, request: RiegoRequest): Result<RiegoResponse> {
        return try {
            val response = RetrofitClient.riegoService.actualizarRiego("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
