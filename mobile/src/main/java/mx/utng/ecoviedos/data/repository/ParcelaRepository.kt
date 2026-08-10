package mx.utng.ecoviedos.data.repository

import android.util.Log
import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

/**
 * Repositorio encargado de gestionar los datos de las parcelas.
 * 
 * Se comunica con la API REST para realizar operaciones CRUD sobre las parcelas.
 */
class ParcelaRepository {

    /**
     * Obtiene todas las parcelas registradas en el sistema.
     * 
     * @param token Token de autenticación del usuario.
     * @return Resultado con la lista de parcelas o el error producido.
     */
    suspend fun obtenerParcelas(token: String): Result<List<Parcela>> {
        return try {
            Log.d("ParcelaRepository", "Llamando a Retrofit: GET /api/parcelas")
            val response = RetrofitClient.parcelaService.obtenerParcelas("Bearer $token")
            Log.d("ParcelaRepository", "Respuesta Retrofit: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val parcelas = response.body()!!.map { it.toDomain() }
                Result.success(parcelas)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ParcelaRepository", "Fallo en la llamada Retrofit", e)
            Result.failure(e)
        }
    }

    /**
     * Registra una nueva parcela en el servidor.
     * 
     * @param token Token de autenticación del usuario.
     * @param request Datos de la parcela a crear.
     * @return Resultado con la parcela creada o el error producido.
     */
    suspend fun crearParcela(token: String, request: ParcelaRequest): Result<Parcela> {
        return try {
            val response = RetrofitClient.parcelaService.crearParcela("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val mensaje = when (response.code()) {
                    401 -> "Tu sesión expiró, vuelve a iniciar sesión"
                    403 -> "No tienes permiso para crear parcelas"
                    else -> "Error del servidor: ${response.code()}"
                }
                Result.failure(Exception(mensaje))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la información de una parcela existente.
     * 
     * @param token Token de autenticación del usuario.
     * @param id Identificador único de la parcela.
     * @param request Datos actualizados de la parcela.
     * @return Resultado con la parcela actualizada o el error producido.
     */
    suspend fun actualizarParcela(token: String, id: String, request: ParcelaRequest): Result<Parcela> {
        return try {
            val response = RetrofitClient.parcelaService.actualizarParcela("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al actualizar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una parcela mediante su identificador.
     * 
     * @param token Token de autenticación del usuario.
     * @param id Identificador de la parcela a eliminar.
     * @return Resultado unitario de éxito o el error producido.
     */
    suspend fun eliminarParcela(token: String, id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.parcelaService.eliminarParcela("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
