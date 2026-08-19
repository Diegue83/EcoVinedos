package mx.utng.ecoviedos.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Petición para el inicio de sesión.
 *
 * @property correo Correo electrónico del usuario.
 * @property contrasena Contraseña de acceso.
 */
data class LoginRequest(
    val correo: String,
    @SerializedName("contraseña") val contrasena: String
)

/**
 * Respuesta del servidor tras un inicio de sesión exitoso.
 *
 * @property _id Identificador único del usuario.
 * @property nombre Nombre completo del usuario.
 * @property correo Email del usuario.
 * @property rol Rol asignado (admin, enologo, trabajador).
 * @property token Token JWT para autorizar peticiones.
 */
data class LoginResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val token: String
)

/**
 * Representación de una Parcela recibida del servidor.
 *
 * Contiene todos los metadatos y mediciones actuales de una parcela.
 */
data class ParcelaResponse(
    val _id: String,
    val nombreParcela: String? = null,
    val variedad: String? = null,
    val areaM2: Double? = 0.0,
    val umbralHumedad: Double? = 0.0,
    val umbralTemp: Double? = 0.0,
    val umbralHumedadSuelo: Double? = 0.0,
    val humedadOptimaSuelo: Double? = 0.0,
    val indiceMaduracion: Double? = 0.0,
    val fechaCosecha: String? = null,
    val activa: Boolean? = true,
    val humedad: Double? = 0.0,
    val temperatura: Double? = 0.0,
    val humedadSuelo: Double? = 0.0,
    val brix: Double? = null,
    val ph: Double? = null,
    val acidez: Double? = null,
    val phSuelo: Double? = null,
    val riegoActivo: Boolean? = false,
    val tiempoRestanteRiego: Int? = 0,
    val consumoAguaM2: Double? = 3.0,
    val tipoRiego: String? = "MANUAL",
    val nodoVinculado: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Datos requeridos para crear o actualizar una parcela.
 */
data class ParcelaRequest(
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Double,
    val umbralHumedad: Double,
    val umbralTemp: Double,
    val umbralHumedadSuelo: Double,
    val humedadOptimaSuelo: Double,
    val activa: Boolean,
    val brix: Int? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null,
    val consumoAguaM2: Double? = 3.0,
    val tipoRiego: String? = "MANUAL",
    val fechaCosecha: String? = null
)

/**
 * Información básica de un usuario del sistema.
 */
data class UsuarioResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val telefono: String? = null,
    val fechaRegistro: String? = null
)

/**
 * Petición para registrar o modificar un usuario.
 */
data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    @SerializedName("contraseña") val contrasena: String? = null,
    val rol: String,
    val telefono: String? = null
)

/**
 * Registro de una actividad en la bitácora.
 */
data class BitacoraResponse(
    val _id: String,
    val parcela: String,
    val usuario: String,
    val accion: String,
    val descripcion: String?,
    val fecha: String?
)

/**
 * Datos para crear un nuevo registro de bitácora.
 */
data class BitacoraRequest(
    val parcela: String,
    val accion: String,
    val descripcion: String?,
    val fecha: String? = null
)

/**
 * Historial de un evento de riego.
 */
data class RiegoResponse(
    val _id: String,
    val parcela: String,
    val fecha: String?,
    val duracion: Int,
    val litros: Int,
    val estado: String
)

/**
 * Petición para programar o registrar un riego.
 */
data class RiegoRequest(
    val parcela: String,
    val duracion: Int,
    val litros: Int,
    val estado: String? = "programado"
)

/**
 * Resultados de una muestra analítica de campo.
 */
data class MuestraResponse(
    val _id: String,
    val parcela: String,
    val brix: Double,
    val ph: Double,
    val acidez: Double,
    val phSuelo: Double,
    val indiceMaduracion: Double? = null,
    val observaciones: String?,
    val fecha: String?,
    val createdAt: String?
)

/**
 * Datos para el registro de una nueva muestra de laboratorio.
 */
data class MuestraRequest(
    val parcelaId: String,
    val brix: Double,
    val ph: Double,
    val acidez: Double,
    val phSuelo: Double,
    val indiceMaduracion: Double? = null,
    val observaciones: String?,
    val fecha: String? = null
)

/**
 * Alerta o aviso del sistema.
 */
data class NotificacionResponse(
    val _id: String,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val parcela: String?,
    val estado: String,
    val fecha: String
)
