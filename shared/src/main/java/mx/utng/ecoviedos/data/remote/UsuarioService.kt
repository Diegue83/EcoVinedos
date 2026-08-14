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
 * Interfaz de Retrofit para los servicios de autenticación y gestión de usuarios.
 *
 * Define las operaciones necesarias para el inicio de sesión, administración de perfiles
 * y procesos de recuperación de contraseña.
 */
interface UsuarioService {

    /**
     * Autentica a un usuario en el sistema.
     *
     * @param request Credenciales de acceso (correo y contraseña).
     * @return [Response] con la información del perfil y el token JWT.
     */
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Obtiene la lista completa de usuarios del sistema (Acceso Admin).
     */
    @GET("api/usuarios")
    suspend fun obtenerUsuarios(@Header("Authorization") token: String): Response<List<UsuarioResponse>>

    /**
     * Consulta el perfil de un usuario específico.
     */
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<UsuarioResponse>

    /**
     * Registra un nuevo usuario en la plataforma.
     */
    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") token: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    /**
     * Actualiza los datos de un usuario existente.
     */
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    /**
     * Elimina una cuenta de usuario.
     */
    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    /**
     * Inicia el flujo de recuperación enviando un código al correo electrónico.
     */
    @POST("api/auth/forgot-password")
    suspend fun solicitarRecuperacion(@Body request: Map<String, String>): Response<Map<String, String>>

    /**
     * Valida si el código ingresado coincide con el enviado por correo.
     */
    @POST("api/auth/verify-code")
    suspend fun verificarCodigo(@Body request: Map<String, String>): Response<Map<String, String>>

    /**
     * Establece una nueva contraseña tras una verificación de código exitosa.
     */
    @POST("api/auth/reset-password")
    suspend fun reestablecerContraseña(@Body request: Map<String, String>): Response<Map<String, String>>
}

