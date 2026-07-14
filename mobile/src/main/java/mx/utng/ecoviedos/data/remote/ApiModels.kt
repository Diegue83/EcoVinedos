package mx.utng.ecoviedos.data.remote

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