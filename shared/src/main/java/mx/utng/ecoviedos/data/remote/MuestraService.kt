package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para la gestión de muestras analíticas de campo.
 *
 * Permite registrar y consultar parámetros de calidad del fruto (Brix, pH, acidez).
 */
interface MuestraService {

    /**
     * Registra una nueva muestra técnica.
     *
     * @param token Token de administrador o enólogo.
     * @param request Parámetros de la muestra.
     * @return Muestra registrada.
     */
    @POST("api/muestras")
    suspend fun registrarMuestra(
        @Header("Authorization") token: String,
        @Body request: MuestraRequest
    ): Response<MuestraResponse>

    /**
     * Obtiene el historial de muestras para una parcela específica.
     *
     * @param token Token de autenticación.
     * @param parcelaId ID de la parcela.
     * @return Lista de muestras históricas.
     */
    @GET("api/muestras/parcela/{parcelaId}")
    suspend fun obtenerHistorialPorParcela(
        @Header("Authorization") token: String,
        @Path("parcelaId") parcelaId: String
    ): Response<List<MuestraResponse>>
}
