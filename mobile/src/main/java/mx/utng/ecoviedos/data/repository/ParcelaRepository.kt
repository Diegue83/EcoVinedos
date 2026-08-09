package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

class ParcelaRepository {

    suspend fun obtenerParcelas(token: String): Result<List<Parcela>> {
        return try {
            val response = RetrofitClient.parcelaService.obtenerParcelas("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val parcelas = response.body()!!.map { it.toDomain() }
                Result.success(parcelas)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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