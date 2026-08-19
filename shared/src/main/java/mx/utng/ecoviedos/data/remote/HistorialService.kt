package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class HistorialSensorResponse(
    val _id: String,
    val parcela: String,
    val humedadAire: Double,
    val temperaturaAire: Double,
    val humedadSuelo: Double,
    val consumoAgua: Double = 0.0,
    val fecha: String
)

data class ResumenDiarioResponse(
    val _id: String,
    val parcela: String,
    val humedadAirePromedio: Double,
    val temperaturaAirePromedio: Double,
    val humedadSueloPromedio: Double,
    val consumoAguaTotal: Double = 0.0,
    val fecha: String
)

/**
 * Interfaz de Retrofit para consultar el historial de telemetría de sensores.
 *
 * Proporciona acceso a los datos históricos acumulados por los nodos IoT.
 */
interface HistorialService {
    /**
     * Obtiene el listado de lecturas granulares de una parcela.
     *
     * @param parcelaId Identificador de la parcela.
     * @param limit Cantidad máxima de registros a devolver.
     * @return Lista de lecturas históricas.
     */
    @GET("api/historial/parcela/{parcelaId}")
    suspend fun obtenerHistorialParcela(
        @Path("parcelaId") parcelaId: String,
        @Query("limit") limit: Int = 100
    ): Response<List<HistorialSensorResponse>>

    /**
     * Recupera el resumen consolidado (promedios diarios) de una parcela.
     *
     * @param parcelaId Identificador de la parcela.
     * @return Lista de promedios por día.
     */
    @GET("api/historial/resumen/{parcelaId}")
    suspend fun obtenerResumenParcela(
        @Path("parcelaId") parcelaId: String
    ): Response<List<ResumenDiarioResponse>>
}
