package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.UsuarioRequest
import mx.utng.ecoviedos.data.remote.UsuarioResponse

class UsuarioRepository {

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
