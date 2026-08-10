package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

class HistorialRepository {
    suspend fun obtenerHistorial(parcelaId: String): Result<List<HistorialSensorResponse>> {
        return try {
            val response = RetrofitClient.historialService.obtenerHistorialParcela(parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener historial"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerResumen(parcelaId: String): Result<List<ResumenDiarioResponse>> {
        return try {
            val response = RetrofitClient.historialService.obtenerResumenParcela(parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener resumen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
