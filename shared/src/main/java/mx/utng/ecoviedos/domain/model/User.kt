package mx.utng.ecoviedos.domain.model

import java.util.Date

enum class UserRole {
    ADMINISTRADOR, ENOLOGO, TRABAJADOR_DE_CAMPO
}

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val telefono: Int,
    val rol: UserRole,
    val createdAt: Date,
    val activo: Boolean
)
