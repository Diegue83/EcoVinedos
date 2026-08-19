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
 * Interfaz de Retrofit para el control y programación de sistemas de riego.
 *
 * Provee acceso al historial de riegos y a la configuración de ciclos automáticos.
 */
interface RiegoService {

    /**
     * Consulta la lista de riegos realizados o programados.
     *
     * @param token Token de sesión.
     * @param parcelaId Filtro por parcela.
     * @param estado Filtro por estado del riego.
     * @return Lista de registros de riego.
     */
    @GET("api/riegos")
    suspend fun obtenerRiegos(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<RiegoResponse>>

    /**
     * Consulta un registro de riego por su ID.
     */
    @GET("api/riegos/{id}")
    suspend fun obtenerRiegoPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<RiegoResponse>

    /**
     * Crea un nuevo registro o programación de riego.
     */
    @POST("api/riegos")
    suspend fun crearRiego(
        @Header("Authorization") token: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    /**
     * Actualiza la información de un riego.
     */
    @PUT("api/riegos/{id}")
    suspend fun actualizarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    /**
     * Elimina un registro de riego.
     */
    @DELETE("api/riegos/{id}")
    suspend fun eliminarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
