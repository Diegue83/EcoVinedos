package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

class MuestraRepository {

    suspend fun registrarMuestra(token: String, request: MuestraRequest): Result<MuestraResponse> {
        return try {
            val response = RetrofitClient.muestraService.registrarMuestra("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar muestra: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerHistorial(token: String, parcelaId: String): Result<List<MuestraResponse>> {
        return try {
            val response = RetrofitClient.muestraService.obtenerHistorialPorParcela("Bearer $token", parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener historial: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
