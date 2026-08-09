package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.BitacoraRequest
import mx.utng.ecoviedos.data.remote.BitacoraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

class BitacoraRemoteRepository {

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