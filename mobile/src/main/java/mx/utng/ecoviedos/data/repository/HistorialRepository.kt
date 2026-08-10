package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la consulta de datos históricos de sensores.
 */
class HistorialRepository {

    /**
     * Obtiene el historial reciente de lecturas (cada 15 min) de una parcela.
     * 
     * @param parcelaId Identificador de la parcela.
     * @return Resultado con la lista de lecturas o error.
     */
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

    /**
     * Obtiene el resumen de promedios diarios (1 año) de una parcela.
     * 
     * @param parcelaId Identificador de la parcela.
     * @return Resultado con la lista de resúmenes diarios o error.
     */
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
