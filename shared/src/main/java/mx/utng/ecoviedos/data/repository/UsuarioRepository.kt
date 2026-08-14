package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.UsuarioRequest
import mx.utng.ecoviedos.data.remote.UsuarioResponse

/**
 * Repositorio encargado de la gestión de usuarios en el sistema.
 *
 * Proporciona métodos para realizar operaciones CRUD sobre los usuarios
 * comunicándose con la API REST a través de [RetrofitClient.usuarioService].
 */
class UsuarioRepository {

    /**
     * Obtiene la lista de todos los usuarios registrados.
     *
     * @param token Token de autenticación del usuario administrador.
     * @return [Result] con la lista de [UsuarioResponse] o un error.
     */
    suspend fun obtenerUsuarios(token: String): Result<List<UsuarioResponse>> {
        return try {
            val response = RetrofitClient.usuarioService.obtenerUsuarios("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener usuarios: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param token Token de autenticación del usuario administrador.
     * @param request Datos del nuevo usuario ([UsuarioRequest]).
     * @return [Result] con el [UsuarioResponse] creado o un error.
     */
    suspend fun crearUsuario(token: String, request: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val response = RetrofitClient.usuarioService.crearUsuario("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param token Token de autenticación del usuario administrador.
     * @param id Identificador único del usuario a actualizar.
     * @param request Datos actualizados del usuario ([UsuarioRequest]).
     * @return [Result] con el [UsuarioResponse] actualizado o un error.
     */
    suspend fun actualizarUsuario(token: String, id: String, request: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val response = RetrofitClient.usuarioService.actualizarUsuario("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param token Token de autenticación del usuario administrador.
     * @param id Identificador único del usuario a eliminar.
     * @return [Result] exitoso (Unit) o un error.
     */
    suspend fun eliminarUsuario(token: String, id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.usuarioService.eliminarUsuario("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

