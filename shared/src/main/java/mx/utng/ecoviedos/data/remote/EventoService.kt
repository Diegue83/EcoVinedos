package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

data class EventoRequest(
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val imagenUrl: String? = null
)

data class EventoResponse(
    val _id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val tipo: String,
    val imagenUrl: String? = null
)

interface EventoService {
    @GET("api/eventos")
    suspend fun obtenerEventos(@Query("tipo") tipo: String? = null): Response<List<EventoResponse>>

    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>
}
