package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interfaz de Retrofit para definir los puntos finales (endpoints) relacionados con la gestión de parcelas.
 *
 * Provee métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar) sobre el recurso Parcela
 * en el servidor backend.
 */
interface ParcelaService {

    /**
     * Recupera todas las parcelas accesibles para el usuario autenticado.
     *
     * @param token Token de autorización con formato "Bearer {token}".
     * @return [Response] que contiene una lista de [ParcelaResponse].
     */
    @GET("api/parcelas")
    suspend fun obtenerParcelas(@Header("Authorization") token: String): Response<List<ParcelaResponse>>

    /**
     * Obtiene los detalles de una parcela específica por su ID.
     *
     * @param token Token de autorización.
     * @param id Identificador único de la parcela.
     * @return [Response] con el objeto [ParcelaResponse] detallado.
     */
    @GET("api/parcelas/{id}")
    suspend fun obtenerParcelaPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ParcelaResponse>

    /**
     * Registra una nueva parcela en el sistema.
     *
     * @param token Token de autorización (requiere privilegios de administrador o técnico).
     * @param parcela Datos de la parcela a crear ([ParcelaRequest]).
     * @return [Response] con la parcela creada exitosamente.
     */
    @POST("api/parcelas")
    suspend fun crearParcela(
        @Header("Authorization") token: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    /**
     * Actualiza una parcela existente en el servidor.
     *
     * @param token Token de autorización.
     * @param id Identificador único de la parcela a modificar.
     * @param parcela Datos actualizados ([ParcelaRequest]).
     * @return [Response] con el objeto actualizado.
     */
    @PUT("api/parcelas/{id}")
    suspend fun actualizarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    /**
     * Elimina una parcela del sistema.
     *
     * @param token Token de autorización.
     * @param id Identificador único de la parcela a eliminar.
     * @return [Response] con código de estado 204 (No Content) en caso de éxito.
     */
    @DELETE("api/parcelas/{id}")
    suspend fun eliminarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}

