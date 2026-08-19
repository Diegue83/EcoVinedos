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

### `shared/build.gradle.kts`
```kotlin
import org.gradle.kotlin.dsl.dependencies

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.ecoviedos.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Retrofit
    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // MQTT
    api(libs.org.eclipse.paho.client.mqttv3)

    // ViewModel
    api(libs.androidx.lifecycle.runtime.ktx)
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}
```

### `shared/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

### `mx.utng.ecoviedos.shared.data.mqtt.MqttConfig.kt`
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

### `mx.utng.ecoviedos.shared.data.mqtt.MqttManager.kt`
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
 * @param context Contexto de la aplicación.
 * @param onMessageReceived Callback para datos de sensores.
 * @param onRiegoStatusReceived Callback para estado de válvulas.
 * @param onParcelListReceived Callback para lista de parcelas.
 * @param onCavaListReceived Callback para lista de cavas.
 * @param onConnectionStatusChanged Callback para estado de conexión.
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
     * Inicia la conexión con el broker MQTT.
     */
    fun connect(customBrokerUrl: String? = null) {
        if (isConnecting) return
        val serverUri = if (!customBrokerUrl.isNullOrBlank() && customBrokerUrl.startsWith("ssl://")) customBrokerUrl else MqttConfig.BROKER_URL
        try {
            isConnecting = true
            onConnectionStatusChanged(false, "Conectando al broker...")
            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                if (MqttConfig.USERNAME.isNotEmpty()) {
                    userName = MqttConfig.USERNAME
                    password = MqttConfig.PASSWORD.toCharArray()
                }
                isAutomaticReconnect = true
                isCleanSession = true
            }
            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    isConnecting = false
                    onConnectionStatusChanged(true, "Conectado")
                    subscribeToTopics()
                }
                override fun connectionLost(cause: Throwable?) {
                    isConnecting = false
                    onConnectionStatusChanged(false, "Sin conexión")
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    when {
                        topic == MqttConfig.TOPIC_PARCELAS_LISTA -> onParcelListReceived(payload)
                        topic == MqttConfig.TOPIC_SECCIONES_LISTA -> onCavaListReceived(payload)
                        topic?.startsWith("vinedo/parcela/") == true && topic.endsWith("/stats") -> handleStatsMessage(topic, payload)
                        topic?.startsWith("vinedo/parcela/") == true && (topic.endsWith("/riego") || topic.endsWith("/control")) -> handleRiegoMessage(topic, payload)
                    }
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            mqttClient?.connect(options)
        } catch (e: Exception) { isConnecting = false }
    }

    private fun handleStatsMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val pId = parts[2]
            try {
                val json = JSONObject(payload)
                val sensores = json.optJSONObject("sensores")
                val hum = sensores?.optDouble("humedad_aire", 0.0)?.toFloat() ?: 0f
                val temp = sensores?.optDouble("temperatura_aire", 0.0)?.toFloat() ?: 0f
                val hSuelo = sensores?.optDouble("humedad_suelo", 0.0)?.toFloat() ?: 0f
                onMessageReceived(pId, hum, temp, hSuelo, json.optBoolean("riegoActivo"), json.optInt("tiempoRestante"))
            } catch (e: Exception) { }
        }
    }

    private fun handleRiegoMessage(topic: String, payload: String) {
        val parts = topic.split("/")
        if (parts.size >= 3) {
            val pId = parts[2]
            try {
                val json = JSONObject(payload)
                val activo = json.optString("comando") == "ON" || json.optString("estado") == "ACTIVO"
                onRiegoStatusReceived(pId, activo, json.optInt("duracion") * 60)
            } catch (e: Exception) { }
        }
    }

    private fun subscribeToTopics() {
        mqttClient?.let {
            if (it.isConnected) {
                it.subscribe(MqttConfig.TOPIC_PARCELAS_LISTA, 1)
                it.subscribe(MqttConfig.TOPIC_SECCIONES_LISTA, 1)
                it.subscribe(MqttConfig.TOPIC_PARCELA_STATS, 1)
                it.subscribe("vinedo/parcela/+/riego", 1)
                it.subscribe("vinedo/parcela/+/control", 1)
            }
        }
    }

    /**
     * Envía comando de riego.
     */
    fun toggleRiego(pId: String, activo: Boolean, dur: Int, modo: String) {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    val payload = JSONObject().apply { put("comando", if (activo) "ON" else "OFF"); put("duracion", dur); put("modo", modo) }.toString()
                    it.publish(String.format(MqttConfig.TOPIC_RIEGO_CONTROL, pId), MqttMessage(payload.toByteArray()).apply { qos = 1 })
                }
            }
        } catch (e: Exception) { }
    }

    /**
     * Desconecta el cliente.
     */
    fun disconnect() { try { mqttClient?.disconnect(); mqttClient?.close() } catch (e: Exception) {} }
}
```

### `mx.utng.ecoviedos.data.remote.ApiModels.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import com.google.gson.annotations.SerializedName

/** Petición login. */
data class LoginRequest(val correo: String, @SerializedName("contraseña") val contrasena: String)
/** Respuesta login. */
data class LoginResponse(val _id: String, val nombre: String, val correo: String, val rol: String, val token: String)
/** DTO Parcela. */
data class ParcelaResponse(val _id: String, val nombreParcela: String?, val variedad: String?, val areaM2: Double?, val umbralHumedad: Double?, val umbralTemp: Double?, val umbralHumedadSuelo: Double?, val humedadOptimaSuelo: Double?, val indiceMaduracion: Double?, val fechaCosecha: String?, val activa: Boolean?, val humedad: Double?, val temperatura: Double?, val humedadSuelo: Double?, val brix: Double?, val ph: Double?, val acidez: Double?, val phSuelo: Double?, val riegoActivo: Boolean?, val tiempoRestanteRiego: Int?, val consumoAguaM2: Double?, val tipoRiego: String?, val nodoVinculado: String?, val createdAt: String?, val updatedAt: String?)
/** Petición Parcela. */
data class ParcelaRequest(val nombreParcela: String, val variedad: String, val areaM2: Double, val umbralHumedad: Double, val umbralTemp: Double, val umbralHumedadSuelo: Double, val humedadOptimaSuelo: Double, val activa: Boolean, val brix: Int? = null, val acidez: Float? = null, val phSuelo: Float? = null, val consumoAguaM2: Double? = 3.0, val tipoRiego: String? = "MANUAL", val fechaCosecha: String? = null)
/** DTO Usuario. */
data class UsuarioResponse(val _id: String, val nombre: String, val correo: String, val rol: String, val telefono: String?, val fechaRegistro: String?)
/** Petición Usuario. */
data class UsuarioRequest(val nombre: String, val correo: String, @SerializedName("contraseña") val contrasena: String?, val rol: String, val telefono: String?)
/** DTO Bitácora. */
data class BitacoraResponse(val _id: String, val parcela: String, val usuario: String, val accion: String, val descripcion: String?, val fecha: String?)
/** Petición Bitácora. */
data class BitacoraRequest(val parcela: String, val accion: String, val descripcion: String?, val fecha: String? = null)
/** DTO Riego. */
data class RiegoResponse(val _id: String, val parcela: String, val fecha: String?, val duracion: Int, val litros: Int, val estado: String)
/** Petición Riego. */
data class RiegoRequest(val parcela: String, val duracion: Int, val litros: Int, val estado: String? = "programado")
/** DTO Muestra. */
data class MuestraResponse(val _id: String, val parcela: String, val brix: Double, val ph: Double, val acidez: Double, val phSuelo: Double, val indiceMaduracion: Double?, val observaciones: String?, val fecha: String?, val createdAt: String?)
/** Petición Muestra. */
data class MuestraRequest(val parcelaId: String, val brix: Double, val ph: Double, val acidez: Double, val phSuelo: Double, val indiceMaduracion: Double?, val observaciones: String?, val fecha: String? = null)
/** DTO Notificación. */
data class NotificacionResponse(val _id: String, val tipo: String, val titulo: String, val mensaje: String, val parcela: String?, val estado: String, val fecha: String)
```

### `mx.utng.ecoviedos.data.remote.BitacoraService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Interfaz de red para bitácoras. */
interface BitacoraService {
    /** Obtiene bitácoras del servidor. */
    @GET("api/bitacoras")
    suspend fun obtenerBitacoras(@Header("Authorization") token: String, @Query("parcela") id: String? = null): Response<List<BitacoraResponse>>
    /** Crea registro en la nube. */
    @POST("api/bitacoras")
    suspend fun crearBitacora(@Header("Authorization") token: String, @Body req: BitacoraRequest): Response<BitacoraResponse>
    /** Borra registro por ID. */
    @DELETE("api/bitacoras/{id}")
    suspend fun eliminarBitacora(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>
}
```

### `mx.utng.ecoviedos.data.remote.CavaService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Interfaz de red para gestión de la bodega. */
interface CavaService {
    /** Lista cavas y secciones. */
    @GET("api/cavas")
    suspend fun obtenerCavas(): Response<List<CavaResponse>>
    /** Crea sección en cava. */
    @POST("api/cavas/secciones")
    suspend fun crearSeccion(@Header("Authorization") token: String, @Body req: SeccionCavaRequest): Response<SeccionCavaResponse>
    /** Actualiza stock de sección. */
    @PUT("api/cavas/secciones/{id}")
    suspend fun actualizarSeccion(@Header("Authorization") token: String, @Path("id") id: String, @Body req: SeccionCavaRequest): Response<SeccionCavaResponse>
}
```

### `mx.utng.ecoviedos.data.remote.EventoService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Interfaz de red para eventos y turismo. */
interface EventoService {
    /** Lista eventos disponibles. */
    @GET("api/eventos")
    suspend fun obtenerEventos(@Query("tipo") tipo: String? = null): Response<List<EventoResponse>>
    /** Crea nuevo evento. */
    @POST("api/eventos")
    suspend fun crearEvento(@Header("Authorization") token: String, @Body req: EventoRequest): Response<EventoResponse>
    /** Actualiza evento existente. */
    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(@Header("Authorization") token: String, @Path("id") id: String, @Body req: EventoRequest): Response<EventoResponse>
    /** Elimina evento. */
    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(@Header("Authorization") token: String, @Path("id") id: String): Response<Map<String, String>>
}
```

### `mx.utng.ecoviedos.data.remote.HistorialService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Consulta de historial de sensores. */
interface HistorialService {
    /** Obtiene lecturas históricas granulares. */
    @GET("api/historial/parcela/{id}")
    suspend fun obtenerHistorialParcela(@Path("id") id: String, @Query("limit") limit: Int = 100): Response<List<HistorialSensorResponse>>
    /** Obtiene promedios acumulados diarios. */
    @GET("api/historial/resumen/{id}")
    suspend fun obtenerResumenParcela(@Path("id") id: String): Response<List<ResumenDiarioResponse>>
}
```

### `mx.utng.ecoviedos.data.remote.MuestraService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Gestión de muestras técnicas de uva. */
interface MuestraService {
    /** Registra muestra de campo. */
    @POST("api/muestras")
    suspend fun registrarMuestra(@Header("Authorization") token: String, @Body req: MuestraRequest): Response<MuestraResponse>
    /** Lista muestras de una parcela específica. */
    @GET("api/muestras/parcela/{id}")
    suspend fun obtenerHistorialPorParcela(@Header("Authorization") token: String, @Path("id") id: String): Response<List<MuestraResponse>>
}
```

### `mx.utng.ecoviedos.data.remote.NotificacionService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Servicio de alertas del sistema. */
interface NotificacionService {
    /** Obtiene notificaciones personales del usuario. */
    @GET("api/notificaciones")
    suspend fun obtenerMisNotificaciones(@Header("Authorization") token: String): Response<List<NotificacionResponse>>
    /** Marca una notificación como leída o descartada. */
    @PUT("api/notificaciones/{id}/estado")
    suspend fun cambiarEstado(@Header("Authorization") token: String, @Path("id") id: String, @Body req: Map<String, String>): Response<NotificacionResponse>
}
```

### `mx.utng.ecoviedos.data.remote.ParcelaService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Gestión central de parcelas y umbrales. */
interface ParcelaService {
    /** Lista parcelas accesibles por rol. */
    @GET("api/parcelas")
    suspend fun obtenerParcelas(@Header("Authorization") token: String): Response<List<ParcelaResponse>>
    /** Crea parcela en sistema. */
    @POST("api/parcelas")
    suspend fun crearParcela(@Header("Authorization") token: String, @Body req: ParcelaRequest): Response<ParcelaResponse>
    /** Actualiza configuración de parcela. */
    @PUT("api/parcelas/{id}")
    suspend fun actualizarParcela(@Header("Authorization") token: String, @Path("id") id: String, @Body req: ParcelaRequest): Response<ParcelaResponse>
    /** Borra parcela físicamente. */
    @DELETE("api/parcelas/{id}")
    suspend fun eliminarParcela(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>
}
```

### `mx.utng.ecoviedos.data.remote.RetrofitClient.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Cliente REST centralizado singleton. */
object RetrofitClient {
    private const val BASE_URL = "https://ecovinedos-1.onrender.com"
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder().addInterceptor(logging).build()
    private val retrofit: Retrofit by lazy { Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build() }

    /** Instancias de servicios disponibles. */
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

### `mx.utng.ecoviedos.data.remote.RiegoService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Interfaz para el control histórico de riegos. */
interface RiegoService {
    /** Lista eventos de riego pasados. */
    @GET("api/riegos")
    suspend fun obtenerRiegos(@Header("Authorization") token: String, @Query("parcela") pId: String? = null, @Query("estado") est: String? = null): Response<List<RiegoResponse>>
    /** Crea registro de activación de riego. */
    @POST("api/riegos")
    suspend fun crearRiego(@Header("Authorization") token: String, @Body req: RiegoRequest): Response<RiegoResponse>
}
```

### `mx.utng.ecoviedos.data.remote.TvService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Servicio de emparejamiento para Smart TV. */
interface TvService {
    /** Genera código temporal de 6 dígitos. */
    @POST("api/tv/pair-code")
    suspend fun getPairingCode(@Body req: PairCodeRequest): Response<TvSessionResponse>
    /** Checa si el administrador ya autorizó el ID. */
    @GET("api/tv/status/{id}")
    suspend fun checkStatus(@Path("id") id: String): Response<TvSessionResponse>
    /** Realiza el vínculo desde el smartphone. */
    @POST("api/tv/link")
    suspend fun linkTV(@Header("Authorization") token: String, @Body req: LinkTvRequest): Response<Map<String, String>>
    /** Rompe el vínculo de sesión. */
    @POST("api/tv/unlink")
    suspend fun unlinkTV(@Body req: PairCodeRequest): Response<Map<String, String>>
}
```

### `mx.utng.ecoviedos.data.remote.UploadService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/** Servicio de carga de archivos multimedia. */
interface UploadService {
    /** Envía imagen al storage del backend. */
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(@Header("Authorization") token: String, @Part img: MultipartBody.Part): Response<UploadResponse>
}
```

### `mx.utng.ecoviedos.data.remote.UsuarioService.kt`
```kotlin
package mx.utng.ecoviedos.data.remote

import retrofit2.Response
import retrofit2.http.*

/** Servicio de autenticación y personal. */
interface UsuarioService {
    /** Inicia sesión con email y pass. */
    @POST("api/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>
    /** Lista usuarios registrados. */
    @GET("api/usuarios")
    suspend fun obtenerUsuarios(@Header("Authorization") token: String): Response<List<UsuarioResponse>>
    /** Crea nueva cuenta de trabajador/enólogo. */
    @POST("api/usuarios")
    suspend fun crearUsuario(@Header("Authorization") token: String, @Body req: UsuarioRequest): Response<UsuarioResponse>
    /** Solicita token por email. */
    @POST("api/auth/forgot-password")
    suspend fun solicitarRecuperacion(@Body req: Map<String, String>): Response<Map<String, String>>
    /** Verifica token enviado. */
    @POST("api/auth/verify-code")
    suspend fun verificarCodigo(@Body req: Map<String, String>): Response<Map<String, String>>
    /** Cambia contraseña finalmente. */
    @POST("api/auth/reset-password")
    suspend fun reestablecerContraseña(@Body req: Map<String, String>): Response<Map<String, String>>
}
```

### `mx.utng.ecoviedos.data.repository.BitacoraRemoteRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.BitacoraRequest
import mx.utng.ecoviedos.data.remote.BitacoraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/** Repositorio de acciones administrativas. */
class BitacoraRemoteRepository {
    /** Carga bitácoras de red. */
    suspend fun obtenerBitacoras(token: String, pId: String? = null): Result<List<BitacoraResponse>> {
        return try {
            val res = RetrofitClient.bitacoraService.obtenerBitacoras("Bearer $token", pId)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Persiste bitácora en nube. */
    suspend fun crearBitacora(token: String, req: BitacoraRequest): Result<BitacoraResponse> {
        return try {
            val res = RetrofitClient.bitacoraService.crearBitacora("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.EventoRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/** Repositorio de experiencias turísticas. */
class EventoRepository {
    private val service = RetrofitClient.eventoService
    /** Carga lista de eventos. */
    suspend fun obtenerEventos(tipo: String? = null): Result<List<EventoResponse>> {
        return try {
            val res = service.obtenerEventos(tipo)
            if (res.isSuccessful) Result.success(res.body() ?: emptyList()) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Registra evento. */
    suspend fun crearEvento(token: String, req: EventoRequest): Result<EventoResponse> {
        return try {
            val res = service.crearEvento("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Modifica evento. */
    suspend fun actualizarEvento(token: String, id: String, req: EventoRequest): Result<EventoResponse> {
        return try {
            val res = service.actualizarEvento("Bearer $token", id, req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Borra evento. */
    suspend fun eliminarEvento(token: String, id: String): Result<Boolean> {
        return try {
            val res = service.eliminarEvento("Bearer $token", id)
            if (res.isSuccessful) Result.success(true) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.HistorialRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/** Repositorio para telemetría acumulada. */
class HistorialRepository {
    /** Carga lecturas de sensores. */
    suspend fun obtenerHistorial(id: String): Result<List<HistorialSensorResponse>> {
        return try {
            val res = RetrofitClient.historialService.obtenerHistorialParcela(id)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Carga promedios diarios. */
    suspend fun obtenerResumen(id: String): Result<List<ResumenDiarioResponse>> {
        return try {
            val res = RetrofitClient.historialService.obtenerResumenParcela(id)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.MuestraRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/** Repositorio de analítica de laboratorio. */
class MuestraRepository {
    /** Guarda muestra de Brix/pH. */
    suspend fun registrarMuestra(token: String, req: MuestraRequest): Result<MuestraResponse> {
        return try {
            val res = RetrofitClient.muestraService.registrarMuestra("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Carga histórico de muestras. */
    suspend fun obtenerHistorial(token: String, id: String): Result<List<MuestraResponse>> {
        return try {
            val res = RetrofitClient.muestraService.obtenerHistorialPorParcela("Bearer $token", id)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.NotificacionRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.NotificacionResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/** Repositorio de alertas del trabajador. */
class NotificacionRepository {
    /** Carga notificaciones de red. */
    suspend fun obtenerMisNotificaciones(token: String): Result<List<NotificacionResponse>> {
        return try {
            val res = RetrofitClient.notificacionService.obtenerMisNotificaciones("Bearer $token")
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Marca alerta como leída. */
    suspend fun cambiarEstado(token: String, id: String, est: String): Result<NotificacionResponse> {
        return try {
            val res = RetrofitClient.notificacionService.cambiarEstado("Bearer $token", id, mapOf("estado" to est))
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Error red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.ParcelaMapper.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.ParcelaResponse
import mx.utng.ecoviedos.domain.model.Parcela
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Parsea fecha ISO del servidor. */
private fun parseFechaIso(f: String?): Date {
    if (f.isNullOrBlank()) return Date()
    return try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(f) ?: Date() } catch (e: Exception) { Date() }
}

/** Transforma DTO ParcelaResponse a Modelo de Dominio Parcela. */
fun ParcelaResponse.toDomain(): Parcela {
    return Parcela(
        id = _id, nombreParcela = nombreParcela ?: "P", variedad = variedad ?: "",
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

### `mx.utng.ecoviedos.data.repository.ParcelaRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela

/** Repositorio central de gestión agrícola. */
class ParcelaRepository {
    /** Obtiene parcelas y mapea a dominio. */
    suspend fun obtenerParcelas(token: String): Result<List<Parcela>> {
        return try {
            val res = RetrofitClient.parcelaService.obtenerParcelas("Bearer $token")
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!.map { it.toDomain() }) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Registra parcela en backend. */
    suspend fun crearParcela(token: String, req: ParcelaRequest): Result<Parcela> {
        return try {
            val res = RetrofitClient.parcelaService.crearParcela("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!.toDomain()) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Modifica parcela. */
    suspend fun actualizarParcela(token: String, id: String, req: ParcelaRequest): Result<Parcela> {
        return try {
            val res = RetrofitClient.parcelaService.actualizarParcela("Bearer $token", id, req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!.toDomain()) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Elimina parcela. */
    suspend fun eliminarParcela(token: String, id: String): Result<Unit> {
        return try {
            val res = RetrofitClient.parcelaService.eliminarParcela("Bearer $token", id)
            if (res.isSuccessful) Result.success(Unit) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.RiegoRemoteRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.RiegoRequest
import mx.utng.ecoviedos.data.remote.RiegoResponse

/** Repositorio de eventos hídricos. */
class RiegoRemoteRepository {
    /** Carga riegos registrados. */
    suspend fun obtenerRiegos(token: String, pId: String? = null, est: String? = null): Result<List<RiegoResponse>> {
        return try {
            val res = RetrofitClient.riegoService.obtenerRiegos("Bearer $token", pId, est)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Registra activación de bomba. */
    suspend fun crearRiego(token: String, req: RiegoRequest): Result<RiegoResponse> {
        return try {
            val res = RetrofitClient.riegoService.crearRiego("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.data.repository.UsuarioRepository.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.UsuarioRequest
import mx.utng.ecoviedos.data.remote.UsuarioResponse

/** Repositorio de gestión de personal. */
class UsuarioRepository {
    /** Carga usuarios de la red. */
    suspend fun obtenerUsuarios(token: String): Result<List<UsuarioResponse>> {
        return try {
            val res = RetrofitClient.usuarioService.obtenerUsuarios("Bearer $token")
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Registra nuevo usuario. */
    suspend fun crearUsuario(token: String, req: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val res = RetrofitClient.usuarioService.crearUsuario("Bearer $token", req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Actualiza perfil de usuario. */
    suspend fun actualizarUsuario(token: String, id: String, req: UsuarioRequest): Result<UsuarioResponse> {
        return try {
            val res = RetrofitClient.usuarioService.actualizarUsuario("Bearer $token", id, req)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
    /** Borra cuenta de usuario. */
    suspend fun eliminarUsuario(token: String, id: String): Result<Unit> {
        return try {
            val res = RetrofitClient.usuarioService.eliminarUsuario("Bearer $token", id)
            if (res.isSuccessful) Result.success(Unit) else Result.failure(Exception("Red"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

### `mx.utng.ecoviedos.domain.model.Parcela.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Modelo central de Parcela. */
data class Parcela(
    val id: String, val nombreParcela: String, val variedad: String, val areaM2: Int, val umbralHumedad: Float,
    val umbralTemp: Float, val umbralHumedadSuelo: Float, val humedadOptimaSuelo: Float, val indiceMaduracion: Float,
    val fechaCosecha: Date?, val activa: Boolean, var humedad: Float = 0f, var temperatura: Float = 0f,
    var humedadSuelo: Float = 0f, var riegoActivo: Boolean = false, var tiempoRestanteRiego: Int = 0,
    val brix: Float? = null, val ph: Float? = null, val acidez: Float? = null, val phSuelo: Float? = null,
    val consumoAguaM2: Float = 3.0f, val tipoRiego: String = "MANUAL", val nodoVinculado: String? = null,
    var lastUpdated: Long = System.currentTimeMillis()
) {
    /** Checa si requiere riego inmediato. */
    fun esHumedadCritica(): Boolean = humedadSuelo < umbralHumedadSuelo && !riegoActivo
}
```

### `mx.utng.ecoviedos.domain.model.User.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Roles de sistema. */
enum class UserRole { ADMINISTRADOR, ENOLOGO, TRABAJADOR_DE_CAMPO }

/** Modelo de dominio de Usuario. */
data class User(val id: Long, val email: String, val username: String, val telefono: Int, val rol: UserRole, val createdAt: Date, val activo: Boolean)
```

### `mx.utng.ecoviedos.domain.model.VinedoEvent.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Modelo de dominio para Eventos. */
data class VinedoEvent(val id: String, val title: String, val description: String, val date: Date, val precio: Double = 0.0, val cupo: Int = 0, val imageUrl: String? = null, val type: String = "EVENT")
```

### `mx.utng.ecoviedos.presentation.admin.TourismViewModel.kt`
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

/** ViewModel compartido para eventos y turismo. */
class TourismViewModel : ViewModel() {
    private val repository = EventoRepository()
    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init { cargarEventos() }
    /** Carga eventos de la red. */
    fun cargarEventos(tipo: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.obtenerEventos(tipo).onSuccess { _eventos.value = it }
            _isLoading.value = false
        }
    }
    /** Registra actividad. */
    fun crearEvento(token: String, req: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.crearEvento(token, req).onSuccess { cargarEventos(); onExito() }
            _isLoading.value = false
        }
    }
    /** Actualiza actividad. */
    fun actualizarEvento(token: String, id: String, req: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.actualizarEvento(token, id, req).onSuccess { cargarEventos(); onExito() }
            _isLoading.value = false
        }
    }
    /** Elimina actividad. */
    fun eliminarEvento(token: String, id: String) {
        viewModelScope.launch { repository.eliminarEvento(token, id).onSuccess { cargarEventos() } }
    }
}
```

---

## Flujo de Funcionamiento
1.  **DataSource:** `RetrofitClient` genera las instancias de los servicios.
2.  **Mapeo:** Los repositorios llaman a los servicios y usan `toDomain()` para sanitizar los datos.
3.  **Estado Vivo:** `MqttManager` se conecta a HiveMQ y inyecta datos directamente en los objetos `Parcela`.

## Ejecución
Este módulo se incluye en los archivos `build.gradle.kts` de las aplicaciones finales mediante `implementation(project(":shared"))`.
