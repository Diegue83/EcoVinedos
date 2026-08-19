package mx.utng.ecoviedos.domain.model

import java.util.Date

/**
 * Roles de usuario permitidos en el sistema.
 */
enum class UserRole {
    ADMINISTRADOR, ENOLOGO, TRABAJADOR_DE_CAMPO
}

/**
 * Modelo de dominio que representa a un Usuario.
 *
 * @property id Identificador numérico.
 * @property email Correo electrónico único.
 * @property username Nombre de usuario para mostrar.
 * @property telefono Número de contacto.
 * @property rol Nivel de acceso.
 * @property createdAt Fecha de creación de la cuenta.
 * @property activo Estado de la cuenta.
 */
data class User(
    val id: Long,
    val email: String,
    val username: String,
    val telefono: Int,
    val rol: UserRole,
    val createdAt: Date,
    val activo: Boolean
)
