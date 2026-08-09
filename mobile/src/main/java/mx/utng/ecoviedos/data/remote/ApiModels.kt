package mx.utng.ecoviedos.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val correo: String,
    @SerializedName("contraseña") val contrasena: String
)

data class LoginResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val token: String
)

data class ParcelaResponse(
    val _id: String,
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Double,
    val umbralHumedad: Double,
    val umbralTemp: Double,
    val indiceMaduracion: Double,
    val fechaCosecha: String?,
    val activa: Boolean,
    val humedad: Double,
    val temperatura: Double,
    val humedadSuelo: Double? = 0.0,
    val brix: Double? = null,
    val ph: Double? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null,
    val createdAt: String?,
    val updatedAt: String?
)

data class ParcelaRequest(
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Double,
    val umbralHumedad: Double,
    val umbralTemp: Double,
    val indiceMaduracion: Double? = 0.0,
    val activa: Boolean,
    val brix: Int? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null
)

data class UsuarioResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val telefono: String? = null,
    val fechaRegistro: String? = null
)

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    @SerializedName("contraseña") val contrasena: String? = null,
    val rol: String,
    val telefono: String? = null
)

data class BitacoraResponse(
    val _id: String,
    val parcela: String, // ID de la parcela
    val usuario: String, // ID del usuario
    val accion: String,
    val descripcion: String?,
    val fecha: String?
)

data class BitacoraRequest(
    val parcela: String,
    val accion: String,
    val descripcion: String?,
    val fecha: String? = null
)

data class RiegoResponse(
    val _id: String,
    val parcela: String,
    val fecha: String?,
    val duracion: Int,
    val litros: Int,
    val estado: String
)

data class RiegoRequest(
    val parcela: String,
    val duracion: Int,
    val litros: Int,
    val estado: String? = "programado"
)

data class MuestraResponse(
    val _id: String,
    val parcela: String,
    val brix: Double,
    val ph: Double,
    val acidez: Double,
    val phSuelo: Double,
    val observaciones: String?,
    val fecha: String?,
    val createdAt: String?
)

data class MuestraRequest(
    val parcelaId: String,
    val brix: Double,
    val ph: Double,
    val acidez: Double,
    val phSuelo: Double,
    val observaciones: String?,
    val fecha: String? = null
)
