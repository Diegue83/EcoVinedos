package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la consulta y análisis de datos históricos capturados por los sensores IoT.
 *
 * Provee acceso a lecturas granulares (recientes) y resúmenes estadísticos diarios para el
 * monitoreo a largo plazo de las condiciones climáticas y de suelo en cada parcela.
 */
class HistorialRepository {

    /**
     * Obtiene el historial detallado de lecturas de sensores de una parcela específica.
     * Generalmente devuelve lecturas cada 15 minutos de los últimos días.
     *
     * @param parcelaId Identificador único de la parcela a consultar.
     * @return [Result] con la lista de [HistorialSensorResponse] con telemetría granular.
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
     * Obtiene un resumen consolidado de promedios diarios para una parcela.
     * Útil para visualizaciones gráficas de tendencias anuales o mensuales.
     *
     * @param parcelaId Identificador único de la parcela.
     * @return [Result] con la lista de [ResumenDiarioResponse].
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

