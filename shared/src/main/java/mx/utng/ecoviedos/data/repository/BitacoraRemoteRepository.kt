package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.BitacoraRequest
import mx.utng.ecoviedos.data.remote.BitacoraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio encargado de la gestión remota de la bitácora de actividades.
 *
 * Actúa como mediador entre la UI y el servicio de red para el registro
 * y consulta de eventos administrativos o técnicos en el viñedo.
 */
class BitacoraRemoteRepository {

    /**
     * Obtiene el listado de entradas de la bitácora desde el servidor.
     *
     * @param token Token de autenticación del usuario.
     * @param parcelaId Identificador de la parcela para filtrar resultados (opcional).
     * @return [Result] con la lista de [BitacoraResponse] o un error.
     */
    suspend fun obtenerBitacoras(token: String, parcelaId: String? = null): Result<List<BitacoraResponse>> {
        return try {
            val response = RetrofitClient.bitacoraService.obtenerBitacoras("Bearer $token", parcelaId)
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
     * Registra una nueva acción en la bitácora del sistema.
     *
     * @param token Token de autenticación.
     * @param request Datos del registro a crear ([BitacoraRequest]).
     * @return [Result] con la respuesta del servidor ([BitacoraResponse]).
     */
    suspend fun crearBitacora(token: String, request: BitacoraRequest): Result<BitacoraResponse> {
        return try {
            val response = RetrofitClient.bitacoraService.crearBitacora("Bearer $token", request)
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
