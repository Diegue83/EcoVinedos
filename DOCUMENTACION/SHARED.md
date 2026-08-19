# Módulo :shared

## Descripción
El módulo `:shared` es una librería central que orquesta toda la lógica de negocio, modelos de datos y servicios de infraestructura del ecosistema EcoViñedos. Su función es garantizar que tanto la aplicación móvil como la de TV y el reloj compartan una única definición de la realidad del viñedo, evitando duplicidad de código y errores de sincronización.

Responsabilidades:
*   **Modelado de Dominio:** Definición de entidades puras (Parcela, Usuario, Evento).
*   **Servicios de Red:** Interfaces de Retrofit para interactuar con la API REST.
*   **Repositorios de Datos:** Lógica de acceso, caché y transformación de datos.
*   **Mensajería en Tiempo Real:** Configuración y gestión del cliente MQTT.
*   **Mappers:** Conversión entre modelos de red (DTOs) y modelos de dominio.

## Tecnologías y Dependencias
*   **Retrofit 2 & OkHttp:** Para comunicación síncrona con el Backend.
*   **Paho MQTT Client:** Para recepción de telemetría de sensores IoT.
*   **Gson:** Para serialización de datos.
*   **Coroutines & Flow:** Para manejo de flujos asíncronos de datos.
*   **Lifecycle ViewModel:** Para lógica de presentación compartida.

## Estructura del Módulo (Todos los archivos)
```text
shared/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── java/mx/utng/ecoviedos/
        ├── data/
        │   ├── mqtt/
        │   │   ├── MqttConfig.kt
        │   │   └── MqttManager.kt
        │   ├── remote/
        │   │   ├── ApiModels.kt
        │   │   ├── BitacoraService.kt
        │   │   ├── CavaService.kt
        │   │   ├── EventoService.kt
        │   │   ├── HistorialService.kt
        │   │   ├── MuestraService.kt
        │   │   ├── NotificacionService.kt
        │   │   ├── ParcelaService.kt
        │   │   ├── RetrofitClient.kt
        │   │   ├── RiegoService.kt
        │   │   ├── TvService.kt
        │   │   ├── UploadService.kt
        │   │   └── UsuarioService.kt
        │   └── repository/
        │       ├── BitacoraRemoteRepository.kt
        │       ├── EventoRepository.kt
        │       ├── HistorialRepository.kt
        │       ├── MuestraRepository.kt
        │       ├── NotificacionRepository.kt
        │       ├── ParcelaMapper.kt
        │       ├── ParcelaRepository.kt
        │       ├── RiegoRemoteRepository.kt
        │       └── UsuarioRepository.kt
        ├── domain/
        │   └── model/
        │       ├── Parcela.kt
        │       ├── User.kt
        │       └── VinedoEvent.kt
        └── presentation/
            └── admin/
                └── TourismViewModel.kt
```

---

## Código Fuente Completo

### `MqttConfig.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/mqtt/MqttConfig.kt`
```kotlin
package mx.utng.ecoviedos.shared.data.mqtt

/**
 * Configuración centralizada para la comunicación MQTT.
 *
 * Contiene la URL del broker, credenciales de autenticación y los tópicos
 * utilizados para la telemetría y control de los dispositivos IoT.
 */
object MqttConfig {
    /** URL del broker HiveMQ con soporte SSL. */
    const val BROKER_URL = "ssl://af91fb1b08fc4acca8986fd93abf0207.s1.eu.hivemq.cloud:8883"
    /** Usuario para la conexión al broker. */
    const val USERNAME = "EcoVinMobile"
    /** Contraseña para la conexión al broker. */
    const val PASSWORD = "ecovin$12#34"
    
    /** Tópico para recibir la lista actualizada de parcelas. */
    const val TOPIC_PARCELAS_LISTA = "vinedo/parcelas/lista"
    /** Tópico para recibir la lista actualizada de secciones de cava. */
    const val TOPIC_SECCIONES_LISTA = "vinedo/secciones/lista"
    /** Patrón de tópico para recibir estadísticas de sensores (+ es comodín para ID de parcela). */
    const val TOPIC_PARCELA_STATS = "vinedo/parcela/+/stats"
    /** Formato de tópico para enviar comandos de control de riego. */
    const val TOPIC_RIEGO_CONTROL = "vinedo/parcela/%s/control"
}
```

### `MqttManager.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/mqtt/MqttManager.kt`
```kotlin
package mx.utng.ecoviedos.shared.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

/**
 * Gestor centralizado de la comunicación MQTT para el ecosistema EcoViñedos.
 *
 * Esta clase encapsula la conexión con el broker de Mosquitto (HiveMQ), la suscripción a tópicos
 * de telemetría y el procesamiento de mensajes de sensores en tiempo real.
 *
 * @param context Contexto de la aplicación necesario para la persistencia.
 * @param onMessageReceived Callback ejecutado al recibir telemetría de una parcela (ID, Humedad Aire, Temperatura, Humedad Suelo, Estado Riego, Tiempo).
 * @param onRiegoStatusReceived Callback ejecutado al recibir cambios explícitos en el estado de las válvulas.
 * @param onParcelListReceived Callback para actualizaciones masivas de la lista de parcelas.
 * @param onCavaListReceived Callback para actualizaciones de sensores en bodega/cava.
 * @param onConnectionStatusChanged Notifica cambios en el estado de la conexión al broker.
 */
class MqttManager(
    context: Context,
    private val onMessageReceived: (parcelId: String, humedad: Float, temp: Float, humedadSuelo: Float, riegoActivo: Boolean, tiempoRestante: Int) -> Unit,
    private val onRiegoStatusReceived: (parcelId: String, activo: Boolean, tiempo: Int) -> Unit,
    private val onParcelListReceived: (jsonPayload: String) -> Unit,
    private val onCavaListReceived: (jsonPayload: String) -> Unit = {},
    private val onConnectionStatusChanged: (isConnected: Boolean, message: String?) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val clientId = "AndroidMobile_${System.currentTimeMillis()}"
    private var isConnecting = false

    /**
     * Establece la conexión con el broker MQTT utilizando las credenciales de [MqttConfig].
     *
     * @param customBrokerUrl URL opcional para pruebas con otros brokers.
     */
    fun connect(customBrokerUrl: String? = null) {
        if (isConnecting) return
        
        val serverUri = if (!customBrokerUrl.isNullOrBlank() && customBrokerUrl.startsWith("ssl://")) {
            customBrokerUrl
        } else {
            MqttConfig.BROKER_URL
        }
        
        try {
            isConnecting = true
            onConnectionStatusChanged(false, "Conectando al broker...")

            mqttClient?.let {
                try {
                    if (it.isConnected) it.disconnect()
                    it.close()
                } catch (e: Exception) { }
            }

            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                if (MqttConfig.USERNAME.isNotEmpty()) {
                    userName = MqttConfig.USERNAME
                    password = MqttConfig.PASSWORD.toCharArray()
                }
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 20
                keepAliveInterval = 60
                
                if (serverUri.startsWith("ssl://")) {
                    sslProperties = java.util.Properties()
                }
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    isConnecting = false
                    Log.d("MQTT", "Conectado al broker: $serverURI")
                    onConnectionStatusChanged(true, "Conectado")
                    subscribeToTopics()
                }

                override fun connectionLost(cause: Throwable?) {
                    isConnecting = false
                    Log.e("MQTT", "Conexión perdida: ${cause?.message}")
                    onConnectionStatusChanged(false, "Sin conexión")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return

                    try {
                        when {
                            topic == MqttConfig.TOPIC_PARCELAS_LISTA -> {
                                onParcelListReceived(payload)
                            }
                            topic == MqttConfig.TOPIC_SECCIONES_LISTA -> {
                                onCavaListReceived(payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true &&
                                    topic.endsWith("/stats") -> {
                                handleStatsMessage(topic, payload)
                            }
                            topic?.startsWith("vinedo/parcela/") == true &&
                                    (topic.endsWith("/riego") || topic.endsWith("/control")) -> {
                                handleRiegoMessage(topic, payload)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("MQTT", "Error procesando mensaje: ${e.message}")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
            
        } catch (e: MqttException) {
            isConnecting = false
            onConnectionStatusChanged(false, "Error MQTT")
        } catch (e: Exception) {
            isConnecting = false
            onConnectionStatusChanged(false, "Error de red")
        }
    }

    /**
     * Procesa los mensajes de estadísticas de sensores.
     *
     * @param topic Tópico de origen (contiene el ID de la parcela).
     * @param payload Cuerpo del mensaje en formato JSON.
     */
    private fun handleStatsMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val sensores = json.optJSONObject("sensores")
                val hum = sensores?.optDouble("humedad_aire", 0.0)?.toFloat() ?: 0f
                val temp = sensores?.optDouble("temperatura_aire", 0.0)?.toFloat() ?: 0f
                val humSuelo = sensores?.optDouble("humedad_suelo", 0.0)?.toFloat() ?: 0f
                val riego = json.optBoolean("riegoActivo", false)
                val tiempo = json.optInt("tiempoRestante", 0)
                
                onMessageReceived(parcelId, hum, temp, humSuelo, riego, tiempo)
            } catch (e: Exception) {
                Log.e("MQTT", "Error parsing stats: ${e.message}")
            }
        }
    }

    /**
     * Procesa los mensajes de estado y control de riego.
     *
     * @param topic Tópico de origen.
     * @param payload Cuerpo del mensaje JSON.
     */
    private fun handleRiegoMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val parcelId = parts[2]
            try {
                val json = JSONObject(payload)
                val comando = json.optString("comando", "")
                val estado = json.optString("estado", "")
                val activo = (comando == "ON" || estado == "ACTIVO")
                val duracionInput = json.optInt("duracion", 0)
                
                val tiempoSegundos = if (duracionInput > 0 && duracionInput < 120) duracionInput * 60 else duracionInput
                
                onRiegoStatusReceived(parcelId, activo, tiempoSegundos)
            } catch (e: Exception) {
                Log.e("MQTT", "Error parsing riego: ${e.message}")
            }
        }
    }

    /**
     * Suscribe el cliente a todos los tópicos necesarios para la operación del sistema.
     */
    private fun subscribeToTopics() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                    it.subscribe(MqttConfig.TOPIC_SECCIONES_LISTA, 1)
                    it.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                    it.subscribe("vinedo/parcela/+/riego", 1)
                    it.subscribe("vinedo/parcela/+/control", 1)
                }
            }
        } catch (e: Exception) { }
    }

    /**
     * Envía un comando de activación o desactivación de riego al broker.
     *
     * @param parcelId Identificador de la parcela a controlar.
     * @param activo True para encender, False para apagar.
     * @param duracionMinutos Tiempo programado (solo para encendido).
     * @param modo Modo de operación ("AUTO" o "MANUAL").
     */
    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int = 1, modo: String = "AUTO") {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    val topic = String.format(MqttConfig.TOPIC_RIEGO_CONTROL, parcelId)
                    val payload = JSONObject().apply {
                        put("comando", if (activo) "ON" else "OFF")
                        put("duracion", duracionMinutos)
                        put("modo", modo)
                    }.toString()
                    Log.d("MQTT", "Publicando a $topic: $payload")
                    it.publish(topic, MqttMessage(payload.toByteArray()).apply { qos = 1 })
                } else {
                    Log.w("MQTT", "No se pudo enviar comando: Cliente desconectado")
                }
            } ?: Log.w("MQTT", "No se pudo enviar comando: Cliente es null")
        } catch (e: Exception) {
            Log.e("MQTT", "Error al publicar comando de riego", e)
        }
    }

    /**
     * Cierra la conexión de forma segura y libera los recursos del cliente MQTT.
     */
    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                it.close()
            }
            mqttClient = null
            Log.d("MQTT", "Cliente desconectado y recursos liberados")
        } catch (e: Exception) { }
    }
}
```

### `ApiModels.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/ApiModels.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Petición para el inicio de sesión.
 */
data class LoginRequest(
    val correo: String,
    @SerializedName("contraseña") val contrasena: String
)

/**
 * Respuesta del servidor tras un inicio de sesión exitoso.
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
```

### `BitacoraService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/BitacoraService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para el servicio de bitácoras de actividades.
 *
 * Permite registrar y consultar las acciones realizadas por el personal en las parcelas.
 */
interface BitacoraService {

    /**
     * Obtiene el listado de bitácoras del sistema.
     *
     * @param token Token de autenticación JWT.
     * @param parcelaId Filtro opcional por identificador de parcela.
     * @return Respuesta con la lista de bitácoras registradas.
     */
    @GET("api/bitacoras")
    suspend fun obtenerBitacoras(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null
    ): Response<List<BitacoraResponse>>

    /**
     * Consulta una entrada específica de la bitácora por su ID.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro de bitácora.
     * @return Respuesta con los detalles de la bitácora.
     */
    @GET("api/bitacoras/{id}")
    suspend fun obtenerBitacoraPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BitacoraResponse>

    /**
     * Crea un nuevo registro en la bitácora de actividades.
     *
     * @param token Token de autenticación.
     * @param request Datos del registro a crear.
     * @return Respuesta con el objeto de bitácora creado.
     */
    @POST("api/bitacoras")
    suspend fun crearBitacora(
        @Header("Authorization") token: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    /**
     * Actualiza un registro existente en la bitácora.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro a modificar.
     * @param request Datos actualizados.
     * @return Respuesta con el objeto modificado.
     */
    @PUT("api/bitacoras/{id}")
    suspend fun actualizarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: BitacoraRequest
    ): Response<BitacoraResponse>

    /**
     * Elimina permanentemente un registro de la bitácora.
     *
     * @param token Token de autenticación.
     * @param id Identificador único del registro a eliminar.
     * @return Respuesta sin contenido en caso de éxito.
     */
    @DELETE("api/bitacoras/{id}")
    suspend fun eliminarBitacora(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
```

### `CavaService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/CavaService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Representa una sección de una cava con sus datos de sensores y capacidad.
 */
data class SeccionCavaResponse(
    val _id: String,
    val cava: String,
    val nombre: String,
    val tipo: String,
    val temperatura: Double,
    val humedad: Double,
    val capacidadBotellas: Int,
    val botellasActuales: Int,
    val sensorId: String?,
    val estado: String,
    val ultimaLectura: String
)

/**
 * Representa una cava principal que agrupa varias secciones.
 */
data class CavaResponse(
    val _id: String,
    val nombre: String,
    val ubicacion: String,
    val descripcion: String?,
    val secciones: List<SeccionCavaResponse> = emptyList()
)

/**
 * Petición para crear o actualizar una Cava principal.
 */
data class CavaRequest(
    val nombre: String,
    val ubicacion: String,
    val descripcion: String? = null
)

/**
 * Petición para crear o actualizar una sección de cava.
 */
data class SeccionCavaRequest(
    val cava: String? = null,
    val nombre: String? = null,
    val tipo: String? = null,
    val capacidadBotellas: Int? = null,
    val botellasActuales: Int? = null,
    val sensorId: String? = null
)

/**
 * Interfaz de Retrofit para la gestión de cavas y secciones.
 */
interface CavaService {
    /**
     * Obtiene el listado de todas las cavas registradas.
     */
    @GET("api/cavas")
    suspend fun obtenerCavas(): Response<List<CavaResponse>>

    /**
     * Registra una nueva sección en una cava existente.
     */
    @POST("api/cavas/secciones")
    suspend fun crearSeccion(
        @Header("Authorization") token: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    /**
     * Actualiza el estado o información de una sección de cava.
     */
    @PUT("api/cavas/secciones/{id}")
    suspend fun actualizarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: SeccionCavaRequest
    ): Response<SeccionCavaResponse>

    /**
     * Elimina una sección específica de la cava.
     */
    @DELETE("api/cavas/secciones/{id}")
    suspend fun eliminarSeccion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
```

### `EventoService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/EventoService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Datos para la creación de un evento.
 */
data class EventoRequest(
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imagenUrl: String? = null,
    val ubicacion: String? = null
)

/**
 * Respuesta del servidor con los detalles del evento.
 */
data class EventoResponse(
    val _id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val tipo: String,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imagenUrl: String? = null,
    val ubicacion: String? = null
)

/**
 * Interfaz de Retrofit para la gestión de eventos de turismo y actividades.
 */
interface EventoService {
    /**
     * Recupera todos los eventos programados.
     */
    @GET("api/eventos")
    suspend fun obtenerEventos(@Query("tipo") tipo: String? = null): Response<List<EventoResponse>>

    /**
     * Crea una nueva actividad turística o evento.
     */
    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    /**
     * Actualiza la información de un evento existente.
     */
    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: EventoRequest
    ): Response<EventoResponse>

    /**
     * Elimina permanentemente un evento del sistema.
     */
    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>
}
```

### `HistorialService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/HistorialService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Lectura individual de sensor capturada en el historial.
 */
data class HistorialSensorResponse(
    val _id: String,
    val parcela: String,
    val humedadAire: Double,
    val temperaturaAire: Double,
    val humedadSuelo: Double,
    val consumoAgua: Double = 0.0,
    val fecha: String
)

/**
 * Resumen de promedios diarios de telemetría.
 */
data class ResumenDiarioResponse(
    val _id: String,
    val parcela: String,
    val humedadAirePromedio: Double,
    val temperaturaAirePromedio: Double,
    val humedadSueloPromedio: Double,
    val consumoAguaTotal: Double = 0.0,
    val fecha: String
)

/**
 * Interfaz de Retrofit para consultar el historial de telemetría de sensores.
 */
interface HistorialService {
    /**
     * Obtiene el listado de lecturas granulares de una parcela.
     */
    @GET("api/historial/parcela/{parcelaId}")
    suspend fun obtenerHistorialParcela(
        @Path("parcelaId") parcelaId: String,
        @Query("limit") limit: Int = 100
    ): Response<List<HistorialSensorResponse>>

    /**
     * Recupera el resumen consolidado (promedios diarios) de una parcela.
     */
    @GET("api/historial/resumen/{parcelaId}")
    suspend fun obtenerResumenParcela(
        @Path("parcelaId") parcelaId: String
    ): Response<List<ResumenDiarioResponse>>
}
```

### `MuestraService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/MuestraService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para la gestión de muestras analíticas de campo.
 */
interface MuestraService {

    /**
     * Registra una nueva muestra técnica.
     */
    @POST("api/muestras")
    suspend fun registrarMuestra(
        @Header("Authorization") token: String,
        @Body request: MuestraRequest
    ): Response<MuestraResponse>

    /**
     * Obtiene el historial de muestras para una parcela específica.
     */
    @GET("api/muestras/parcela/{parcelaId}")
    suspend fun obtenerHistorialPorParcela(
        @Header("Authorization") token: String,
        @Path("parcelaId") parcelaId: String
    ): Response<List<MuestraResponse>>
}
```

### `NotificacionService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/NotificacionService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para la gestión de notificaciones push y alertas.
 */
interface NotificacionService {
    /**
     * Recupera todas las notificaciones dirigidas al usuario actual.
     */
    @GET("api/notificaciones")
    suspend fun obtenerMisNotificaciones(
        @Header("Authorization") token: String
    ): Response<List<NotificacionResponse>>

    /**
     * Cambia el estado de una notificación.
     */
    @PUT("api/notificaciones/{id}/estado")
    suspend fun cambiarEstado(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<NotificacionResponse>
}
```

### `ParcelaService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/ParcelaService.kt`
```kotlin
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
 * Interfaz de Retrofit para definir los puntos finales relacionados con la gestión de parcelas.
 */
interface ParcelaService {

    /**
     * Recupera todas las parcelas accesibles para el usuario autenticado.
     */
    @GET("api/parcelas")
    suspend fun obtenerParcelas(@Header("Authorization") token: String): Response<List<ParcelaResponse>>

    /**
     * Obtiene los detalles de una parcela específica por su ID.
     */
    @GET("api/parcelas/{id}")
    suspend fun obtenerParcelaPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ParcelaResponse>

    /**
     * Registra una nueva parcela en el sistema.
     */
    @POST("api/parcelas")
    suspend fun crearParcela(
        @Header("Authorization") token: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    /**
     * Actualiza una parcela existente en el servidor.
     */
    @PUT("api/parcelas/{id}")
    suspend fun actualizarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body parcela: ParcelaRequest
    ): Response<ParcelaResponse>

    /**
     * Elimina una parcela del sistema.
     */
    @DELETE("api/parcelas/{id}")
    suspend fun eliminarParcela(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
```

### `RetrofitClient.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/RetrofitClient.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente centralizado para la configuración y provisión de servicios REST mediante Retrofit.
 */
object RetrofitClient {
    /** URL base del servidor backend. */
    private const val BASE_URL = "https://ecovinedos-1.onrender.com"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Servicios inyectables. */
    val parcelaService: ParcelaService by lazy { retrofit.create(ParcelaService::class.java) }
    val usuarioService: UsuarioService by lazy { retrofit.create(UsuarioService::class.java) }
    val bitacoraService: BitacoraService by lazy { retrofit.create(BitacoraService::class.java) }
    val riegoService: RiegoService by lazy { retrofit.create(RiegoService::class.java) }
    val muestraService: MuestraService by lazy { retrofit.create(MuestraService::class.java) }
    val historialService: HistorialService by lazy { retrofit.create(HistorialService::class.java) }
    val notificacionService: NotificacionService by lazy { retrofit.create(NotificacionService::class.java) }
    val eventoService: EventoService by lazy { retrofit.create(EventoService::class.java) }
    val tvService: TvService by lazy { retrofit.create(TvService::class.java) }
    val cavaService: CavaService by lazy { retrofit.create(CavaService::class.java) }
    val uploadService: UploadService by lazy { retrofit.create(UploadService::class.java) }
}
```

### `RiegoService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/RiegoService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para el control y programación de sistemas de riego.
 */
interface RiegoService {

    /**
     * Consulta la lista de riegos realizados o programados.
     */
    @GET("api/riegos")
    suspend fun obtenerRiegos(
        @Header("Authorization") token: String,
        @Query("parcela") parcelaId: String? = null,
        @Query("estado") estado: String? = null
    ): Response<List<RiegoResponse>>

    /**
     * Consulta un registro de riego por su ID.
     */
    @GET("api/riegos/{id}")
    suspend fun obtenerRiegoPorId(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<RiegoResponse>

    /**
     * Crea un nuevo registro o programación de riego.
     */
    @POST("api/riegos")
    suspend fun crearRiego(
        @Header("Authorization") token: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    /**
     * Actualiza la información de un riego.
     */
    @PUT("api/riegos/{id}")
    suspend fun actualizarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: RiegoRequest
    ): Response<RiegoResponse>

    /**
     * Elimina un registro de riego.
     */
    @DELETE("api/riegos/{id}")
    suspend fun eliminarRiego(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
```

### `TvService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/TvService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para la sincronización y vinculación con Android TV.
 */
interface TvService {
    @POST("api/tv/pair-code")
    suspend fun getPairingCode(@Body request: PairCodeRequest): Response<TvSessionResponse>

    @GET("api/tv/status/{deviceId}")
    suspend fun checkStatus(@Path("deviceId") deviceId: String): Response<TvSessionResponse>

    @POST("api/tv/link")
    suspend fun linkTV(
        @Header("Authorization") token: String,
        @Body request: LinkTvRequest
    ): Response<Map<String, String>>

    @POST("api/tv/unlink")
    suspend fun unlinkTV(@Body request: PairCodeRequest): Response<Map<String, String>>
}
```

### `UploadService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/UploadService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Modelo de respuesta tras una carga exitosa de archivo.
 */
data class UploadResponse(
    val imageUrl: String
)

/**
 * Interfaz de Retrofit para el servicio de carga de archivos (Imágenes).
 */
interface UploadService {
    /**
     * Sube una imagen al servidor.
     */
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
}
```

### `UsuarioService.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/remote/UsuarioService.kt`
```kotlin
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
 */
interface UsuarioService {

    /**
     * Autentica a un usuario en el sistema.
     */
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Obtiene la lista completa de usuarios del sistema.
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
     * Inicia el flujo de recuperación de contraseña.
     */
    @POST("api/auth/forgot-password")
    suspend fun solicitarRecuperacion(@Body request: Map<String, String>): Response<Map<String, String>>

    /**
     * Valida el código de recuperación.
     */
    @POST("api/auth/verify-code")
    suspend fun verificarCodigo(@Body request: Map<String, String>): Response<Map<String, String>>

    /**
     * Establece una nueva contraseña.
     */
    @POST("api/auth/reset-password")
    suspend fun reestablecerContraseña(@Body request: Map<String, String>): Response<Map<String, String>>
}
```

### `BitacoraRemoteRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/BitacoraRemoteRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.BitacoraRequest
import mx.utng.ecoviedos.data.remote.BitacoraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio encargado de la gestión remota de la bitácora de actividades.
 */
class BitacoraRemoteRepository {

    /**
     * Obtiene el listado de entradas de la bitácora desde el servidor.
     */
    suspend fun obtenerBitacoras(token: String, parcelaId: String? = null): Result<List<BitacoraResponse>> {
        return try {
            val response = RetrofitClient.bitacoraService.obtenerBitacoras("Bearer $token", parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra una nueva acción en la bitácora.
     */
    suspend fun crearBitacora(token: String, request: BitacoraRequest): Result<BitacoraResponse> {
        return try {
            val response = RetrofitClient.bitacoraService.crearBitacora("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `EventoRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/EventoRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de eventos de turismo y actividades en el viñedo.
 */
class EventoRepository {
    private val service = RetrofitClient.eventoService

    /**
     * Obtiene una lista de eventos.
     */
    suspend fun obtenerEventos(tipo: String? = null): Result<List<EventoResponse>> {
        return try {
            val response = service.obtenerEventos(tipo)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener eventos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo evento.
     */
    suspend fun crearEvento(token: String, request: EventoRequest): Result<EventoResponse> {
        return try {
            val response = service.crearEvento("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear evento"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un evento existente.
     */
    suspend fun eliminarEvento(token: String, id: String): Result<Boolean> {
        return try {
            val response = service.eliminarEvento("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un evento existente.
     */
    suspend fun actualizarEvento(token: String, id: String, request: EventoRequest): Result<EventoResponse> {
        return try {
            val response = service.actualizarEvento("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar evento"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `HistorialRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/HistorialRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la consulta y análisis de datos históricos capturados por los sensores IoT.
 */
class HistorialRepository {

    /**
     * Obtiene el historial detallado de lecturas.
     */
    suspend fun obtenerHistorial(parcelaId: String): Result<List<HistorialSensorResponse>> {
        return try {
            val response = RetrofitClient.historialService.obtenerHistorialParcela(parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener historial"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene un resumen consolidado de promedios diarios.
     */
    suspend fun obtenerResumen(parcelaId: String): Result<List<ResumenDiarioResponse>> {
        return try {
            val response = RetrofitClient.historialService.obtenerResumenParcela(parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener resumen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `MuestraRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/MuestraRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de muestras analíticas y de laboratorio en las parcelas.
 */
class MuestraRepository {

    /**
     * Registra una nueva muestra técnica.
     */
    suspend fun registrarMuestra(token: String, request: MuestraRequest): Result<MuestraResponse> {
        return try {
            val response = RetrofitClient.muestraService.registrarMuestra("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar muestra: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera el historial cronológico de muestras.
     */
    suspend fun obtenerHistorial(token: String, parcelaId: String): Result<List<MuestraResponse>> {
        return try {
            val response = RetrofitClient.muestraService.obtenerHistorialPorParcela("Bearer $token", parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener historial: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `NotificacionRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/NotificacionRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.NotificacionResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de notificaciones del sistema.
 */
class NotificacionRepository {

    /**
     * Obtiene las notificaciones del usuario autenticado.
     */
    suspend fun obtenerMisNotificaciones(token: String): Result<List<NotificacionResponse>> {
        return try {
            val response = RetrofitClient.notificacionService.obtenerMisNotificaciones("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cambia el estado de una notificación.
     */
    suspend fun cambiarEstado(token: String, id: String, estado: String): Result<NotificacionResponse> {
        return try {
            val response = RetrofitClient.notificacionService.cambiarEstado(
                "Bearer $token", 
                id, 
                mapOf("estado" to estado)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cambiar estado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `ParcelaMapper.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/ParcelaMapper.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.ParcelaResponse
import mx.utng.ecoviedos.domain.model.Parcela
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parsea una cadena de fecha en formato ISO a un objeto [Date].
 */
private fun parseFechaIso(fecha: String?): Date {
    if (fecha.isNullOrBlank()) return Date()
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        formato.parse(fecha) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

/**
 * Convierte un objeto de respuesta de la API a un modelo de dominio de Parcela.
 */
fun ParcelaResponse.toDomain(): Parcela {
    return Parcela(
        id = _id,
        nombreParcela = nombreParcela ?: "Parcela", variedad = variedad ?: "",
        areaM2 = (areaM2 ?: 0.0).toInt(), umbralHumedad = (umbralHumedad ?: 30.0).toFloat(),
        umbralTemp = (umbralTemp ?: 25.0).toFloat(), umbralHumedadSuelo = (umbralHumedadSuelo ?: 40.0).toFloat(),
        humedadOptimaSuelo = (humedadOptimaSuelo ?: 70.0).toFloat(), indiceMaduracion = (indiceMaduracion ?: 0.0).toFloat(),
        fechaCosecha = parseFechaIso(fechaCosecha), activa = activa ?: true,
        humedad = (humedad ?: 0.0).toFloat(), temperatura = (temperatura ?: 0.0).toFloat(),
        humedadSuelo = (humedadSuelo ?: 0.0).toFloat(), riegoActivo = riegoActivo ?: false,
        tiempoRestanteRiego = (tiempoRestanteRiego ?: 0) * 60, brix = brix?.toFloat(),
        ph = ph?.toFloat(), acidez = acidez?.toFloat(), phSuelo = phSuelo?.toFloat(),
        consumoAguaM2 = (consumoAguaM2 ?: 3.0).toFloat(), tipoRiego = (tipoRiego ?: "MANUAL").uppercase(),
        nodoVinculado = nodoVinculado
    )
}
```

### `ParcelaRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/ParcelaRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import android.util.Log
import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

/**
 * Repositorio encargado de gestionar los datos de las parcelas.
 */
class ParcelaRepository {

    /**
     * Obtiene todas las parcelas registradas.
     */
    suspend fun obtenerParcelas(token: String): Result<List<Parcela>> {
        return try {
            val response = RetrofitClient.parcelaService.obtenerParcelas("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val parcelas = response.body()!!.map { it.toDomain() }
                Result.success(parcelas)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra una nueva parcela.
     */
    suspend fun crearParcela(token: String, request: ParcelaRequest): Result<Parcela> {
        return try {
            val response = RetrofitClient.parcelaService.crearParcela("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al crear parcela"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la información de una parcela.
     */
    suspend fun actualizarParcela(token: String, id: String, request: ParcelaRequest): Result<Parcela> {
        return try {
            val response = RetrofitClient.parcelaService.actualizarParcela("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al actualizar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una parcela.
     */
    suspend fun eliminarParcela(token: String, id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.parcelaService.eliminarParcela("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `UsuarioRepository.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/data/repository/UsuarioRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.UsuarioRequest
import mx.utng.ecoviedos.data.remote.UsuarioResponse

/**
 * Repositorio encargado de la gestión de usuarios en el sistema.
 */
class UsuarioRepository {

    /** Obtiene todos los usuarios. */
    suspend fun obtenerUsuarios(token: String): Result<List<UsuarioResponse>> {
        return try {
            val response = RetrofitClient.usuarioService.obtenerUsuarios("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Crea un nuevo usuario. */
    suspend fun crearUsuario(token: String, request: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val response = RetrofitClient.usuarioService.crearUsuario("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Actualiza perfil. */
    suspend fun actualizarUsuario(token: String, id: String, request: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val response = RetrofitClient.usuarioService.actualizarUsuario("Bearer $token", id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Elimina usuario. */
    suspend fun eliminarUsuario(token: String, id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.usuarioService.eliminarUsuario("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### `Parcela.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/domain/model/Parcela.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/**
 * Modelo de dominio que representa una Parcela.
 */
data class Parcela(
    val id: String,
    val nombreParcela: String,
    val variedad: String,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val umbralHumedadSuelo: Float,
    val humedadOptimaSuelo: Float,
    val indiceMaduracion: Float,
    val fechaCosecha: Date?,
    val activa: Boolean,
    var humedad: Float = 0f,
    var temperatura: Float = 0f,
    var humedadSuelo: Float = 0f,
    var riegoActivo: Boolean = false,
    var tiempoRestanteRiego: Int = 0,
    val brix: Float? = null,
    val ph: Float? = null,
    val acidez: Float? = null,
    val phSuelo: Float? = null,
    val consumoAguaM2: Float = 3.0f,
    val tipoRiego: String = "MANUAL",
    val nodoVinculado: String? = null,
    var lastUpdated: Long = System.currentTimeMillis()
) {
    /** Determina si la humedad es crítica. */
    fun esHumedadCritica(): Boolean = humedadSuelo < umbralHumedadSuelo && !riegoActivo
}
```

### `User.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/domain/model/User.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Roles permitidos. */
enum class UserRole { ADMINISTRADOR, ENOLOGO, TRABAJADOR_DE_CAMPO }

/** Modelo de dominio para Usuario. */
data class User(
    val id: Long,
    val email: String,
    val username: String,
    val telefono: Int,
    val rol: UserRole,
    val createdAt: Date,
    val activo: Boolean
)
```

### `VinedoEvent.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/domain/model/VinedoEvent.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Modelo de dominio para Eventos. */
data class VinedoEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    val precio: Double = 0.0,
    val cupo: Int = 0,
    val imageUrl: String? = null,
    val type: String = "EVENT"
)
```

### `TourismViewModel.kt`
Ubicación: `shared/src/main/java/mx/utng/ecoviedos/presentation/admin/TourismViewModel.kt`
```kotlin
package mx.utng.ecoviedos.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.repository.EventoRepository

/**
 * ViewModel compartido para la gestión de actividades y eventos turísticos.
 */
class TourismViewModel : ViewModel() {
    private val repository = EventoRepository()

    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init { cargarEventos() }

    /** Carga todos los eventos. */
    fun cargarEventos(tipo: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.obtenerEventos(tipo).onSuccess { _eventos.value = it }
            _isLoading.value = false
        }
    }

    /** Crea un nuevo evento. */
    fun crearEvento(token: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.crearEvento(token, request).onSuccess {
                cargarEventos()
                onExito()
            }
            _isLoading.value = false
        }
    }

    /** Actualiza un evento. */
    fun actualizarEvento(token: String, id: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.actualizarEvento(token, id, request).onSuccess {
                cargarEventos()
                onExito()
            }
            _isLoading.value = false
        }
    }

    /** Elimina actividad. */
    fun eliminarEvento(token: String, id: String) {
        viewModelScope.launch { repository.eliminarEvento(token, id).onSuccess { cargarEventos() } }
    }
}
```
