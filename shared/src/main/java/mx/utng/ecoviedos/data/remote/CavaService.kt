package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Representa una sección de una cava con sus datos de sensores y capacidad.
 */
data class SeccionCavaResponse(
    val _id: String,
    val cava: String,
    val nombre: String,
    val tipo: String,
    val temperatura: Double,
    val humedad: Double,
    val capacidadBotellas: Int,
    val botellasActuales: Int,
    val sensorId: String?,
    val estado: String,
    val ultimaLectura: String
)

/**
 * Representa una cava principal que agrupa varias secciones.
 */
data class CavaResponse(
    val _id: String,
    val nombre: String,
    val ubicacion: String,
    val descripcion: String?,
    val secciones: List<SeccionCavaResponse> = emptyList()
)

/**
 * Petición para crear o actualizar una Cava principal.
 */
data class CavaRequest(
    val nombre: String,
    val ubicacion: String,
    val descripcion: String? = null
)

/**
 * Petición para crear o actualizar una sección de cava.
 */
data class SeccionCavaRequest(
    val cava: String? = null, // ID de la cava padre
    val nombre: String? = null,
    val tipo: String? = null,
    val capacidadBotellas: Int? = null,
    val botellasActuales: Int? = null,
    val sensorId: String? = null
)

/**
 * Interfaz de Retrofit para la gestión de cavas y secciones.
 */
interface CavaService {
    @GET("api/cavas")
    suspend fun obtenerCavas(): Response<List<CavaResponse>>

    @POST("api/cavas")
    suspend fun crearCava(
        @Header("Authorization") token: String,
        @Body request: CavaRequest
    ): Response<CavaResponse>

    @DELETE("api/cavas/{id}")
    suspend fun eliminarCava(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/cavas/secciones")
    suspend fun crearSeccion(
        @Header("Authorization") token: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    @PUT("api/cavas/secciones/{id}")
    suspend fun actualizarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    @DELETE("api/cavas/secciones/{id}")
    suspend fun eliminarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
