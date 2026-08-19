package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para el servicio de bitácoras de actividades.
 *
 * Permite registrar y consultar las acciones realizadas por el personal en las parcelas.
 */
interface BitacoraService {

    /**
     * Obtiene el listado de bitácoras del sistema.
     *
     * @param token Token de autenticación JWT.
     * @param parcelaId Filtro opcional por identificador de parcela.
     * @return Respuesta con la lista de bitácoras registradas.
     */
    @GET("api/bitacoras")
    suspend fun obtenerBitacoras(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null
    ): Response<List<BitacoraResponse>>

    /**
     * Consulta una entrada específica de la bitácora por su ID.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro de bitácora.
     * @return Respuesta con los detalles de la bitácora.
     */
    @GET("api/bitacoras/{id}")
    suspend fun obtenerBitacoraPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BitacoraResponse>

    /**
     * Crea un nuevo registro en la bitácora de actividades.
     *
     * @param token Token de autenticación.
     * @param request Datos del registro a crear.
     * @return Respuesta con el objeto de bitácora creado.
     */
    @POST("api/bitacoras")
    suspend fun crearBitacora(
        @Header("Authorization") token: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    /**
     * Actualiza un registro existente en la bitácora.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro a modificar.
     * @param request Datos actualizados.
     * @return Respuesta con el objeto modificado.
     */
    @PUT("api/bitacoras/{id}")
    suspend fun actualizarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    /**
     * Elimina permanentemente un registro de la bitácora.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro a eliminar.
     * @return Respuesta sin contenido en caso de éxito.
     */
    @DELETE("api/bitacoras/{id}")
    suspend fun eliminarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
