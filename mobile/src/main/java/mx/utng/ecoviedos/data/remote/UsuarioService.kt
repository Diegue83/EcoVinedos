package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(@Header("Authorization") token: String): Response<List<UsuarioResponse>>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<UsuarioResponse>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") token: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/auth/forgot-password")
    suspend fun solicitarRecuperacion(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/verify-code")
    suspend fun verificarCodigo(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/reset-password")
    suspend fun reestablecerContraseña(@Body request: Map<String, String>): Response<Map<String, String>>
}
