package mx.utng.ecoviedos.data.repository

import android.util.Log
import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

/**
 * Repositorio encargado de gestionar los datos de las parcelas en el ecosistema EcoViñedos.
 *
 * Esta clase actúa como mediadora entre la capa de presentación y la API REST,
 * encapsulando la lógica de red y la transformación de modelos de datos a modelos de dominio.
 * Utiliza [RetrofitClient.parcelaService] para realizar las peticiones al servidor.
 */
class ParcelaRepository {

    /**
     * Obtiene todas las parcelas registradas asociadas al usuario autenticado.
     *
     * @param token Token de autenticación JWT del usuario.
     * @return [Result] que contiene la lista de objetos [Parcela] (modelo de dominio) en caso de éxito,
     * o una excepción en caso de error de red o del servidor.
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
     * Registra una nueva parcela en el servidor central.
     *
     * @param token Token de autenticación del usuario administrador o técnico.
     * @param request Objeto [ParcelaRequest] con los detalles técnicos y de configuración de la parcela.
     * @return [Result] con el objeto [Parcela] creado y procesado por el dominio, o un error descriptivo.
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
     * Actualiza la información y umbrales de una parcela existente.
     *
     * @param token Token de autenticación.
     * @param id Identificador único de la parcela en la base de datos.
     * @param request Datos actualizados encapsulados en un [ParcelaRequest].
     * @return [Result] con la instancia de [Parcela] actualizada.
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
     * Elimina permanentemente una parcela del sistema.
     *
     * @param token Token de autenticación.
     * @param id Identificador único de la parcela a eliminar.
     * @return [Result] exitoso (Unit) si la operación fue confirmada por el servidor, o un error.
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

