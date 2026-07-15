package mx.utng.ecoviedos.data.remote

data class LoginRequest(
    val correo: String,
    val contraseña: String
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
    val nombre: String,
    val ubicacion: String,
    val superficie: Double,
    val cultivo: String?,
    val humedad: Double,
    val temperatura: Double,
    val estado: String,
    val umbralHumedad: Double?,
    val umbralTemp: Double?,
    val indiceMaduracion: Double?,
    val fechaCosecha: String?,
    val responsable: UsuarioResponse?,
    val fechaRegistro: String?
)

data class UsuarioResponse(
    val _id: String,
    val nombre: String,
    val correo: String,
    val rol: String
)

data class ParcelaRequest(
    val nombre: String,
    val ubicacion: String,
    val superficie: Double,
    val cultivo: String?,
    val humedad: Double?,
    val temperatura: Double?,
    val estado: String?,
    val umbralHumedad: Double?,
    val umbralTemp: Double?,
    val responsable: String
)

data class ParcelaResumen(
    val _id: String,
    val nombre: String,
    val ubicacion: String?
)

data class BitacoraResponse(
    val _id: String,
    val parcela: ParcelaResumen?,
    val usuario: UsuarioResponse?,
    val accion: String,
    val descripcion: String?,
    val fecha: String?
)

data class BitacoraRequest(
    val parcela: String,
    val accion: String,
    val descripcion: String?
)

data class RiegoResponse(
    val _id: String,
    val parcela: ParcelaResumen?,
    val fecha: String?,
    val duracion: Double,
    val litros: Double,
    val estado: String
)

data class RiegoRequest(
    val parcela: String,
    val duracion: Double,
    val litros: Double,
    val estado: String?
)