package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

data class EventoRequest(
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imagenUrl: String? = null,
    val ubicacion: String? = null
)

data class EventoResponse(
    val _id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val tipo: String,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imagenUrl: String? = null,
    val ubicacion: String? = null
)

/**
 * Interfaz de Retrofit para la gestión de eventos de turismo y actividades.
 *
 * Facilita el acceso al catálogo de experiencias turísticas y eventos especiales del viñedo.
 */
interface EventoService {
    /**
     * Recupera todos los eventos programados.
     *
     * @param tipo Filtro opcional por categoría (e.g., "EVENT", "TOURISM").
     * @return Lista de eventos encontrados.
     */
    @GET("api/eventos")
    suspend fun obtenerEventos(@Query("tipo") tipo: String? = null): Response<List<EventoResponse>>

    /**
     * Crea una nueva actividad turística o evento.
     *
     * @param token Token de administrador.
     * @param request Datos de la actividad.
     * @return Actividad creada.
     */
    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    /**
     * Actualiza la información de un evento existente.
     *
     * @param token Token de administrador.
     * @param id Identificador único del evento.
     * @param request Nuevos datos.
     * @return Evento actualizado.
     */
    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    /**
     * Elimina permanentemente un evento del sistema.
     *
     * @param token Token de administrador.
     * @param id Identificador del evento a borrar.
     * @return Mensaje de confirmación.
     */
    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>
}
