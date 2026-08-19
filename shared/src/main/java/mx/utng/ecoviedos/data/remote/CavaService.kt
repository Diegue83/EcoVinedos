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
/**
 * Petición para crear o actualizar una sección de cava.
 *
 * @property cava ID de la cava padre.
 * @property nombre Nombre descriptivo de la sección.
 * @property tipo Material o uso de la sección (e.g., ROBLE, ACERO).
 * @property capacidadBotellas Límite máximo de almacenamiento.
 * @property botellasActuales Conteo actual de botellas.
 * @property sensorId Identificador del sensor IoT vinculado.
 */
data class SeccionCavaRequest(
    val cava: String? = null,
    val nombre: String? = null,
    val tipo: String? = null,
    val capacidadBotellas: Int? = null,
    val botellasActuales: Int? = null,
    val sensorId: String? = null
)

/**
 * Interfaz de Retrofit para la gestión de cavas y secciones.
 *
 * Provee acceso a la estructura física de la bodega y sus condiciones ambientales.
 */
interface CavaService {
    /**
     * Obtiene el listado de todas las cavas registradas.
     *
     * @return Lista de cavas con sus secciones incluidas.
     */
    @GET("api/cavas")
    suspend fun obtenerCavas(): Response<List<CavaResponse>>

    /**
     * Registra una nueva sección en una cava existente.
     *
     * @param token Token de autenticación.
     * @param request Datos de la sección a crear.
     * @return Sección creada.
     */
    @POST("api/cavas/secciones")
    suspend fun crearSeccion(
        @Header("Authorization") token: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    /**
     * Actualiza el estado o información de una sección de cava.
     *
     * @param token Token de autenticación.
     * @param id Identificador de la sección.
     * @param request Datos a actualizar (e.g., stock de botellas).
     * @return Sección actualizada.
     */
    @PUT("api/cavas/secciones/{id}")
    suspend fun actualizarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    /**
     * Elimina una sección específica de la cava.
     *
     * @param token Token de autenticación.
     * @param id Identificador de la sección.
     * @return Éxito de la operación.
     */
    @DELETE("api/cavas/secciones/{id}")
    suspend fun eliminarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
