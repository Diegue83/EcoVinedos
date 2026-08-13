package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.RiegoRequest
import mx.utng.ecoviedos.data.remote.RiegoResponse

class RiegoRemoteRepository {

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
