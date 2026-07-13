package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

class ParcelaRepository {

    suspend fun obtenerParcelas(token: String): Result<List<Parcela>> {
        return try {
            val response = RetrofitClient.instance.obtenerParcelas("Bearer $token")
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
            val response = RetrofitClient.instance.crearParcela("Bearer $token", request)
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
}