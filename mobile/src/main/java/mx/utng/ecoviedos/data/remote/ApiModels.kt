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
    val createdAt: String?,
    val updatedAt: String?
)

data class UsuarioResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class ParcelaRequest(
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Double,
    val umbralHumedad: Double,
    val umbralTemp: Double,
    val activa: Boolean,
)