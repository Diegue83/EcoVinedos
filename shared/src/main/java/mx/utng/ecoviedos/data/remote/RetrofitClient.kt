package mx.utng.ecoviedos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente centralizado para la configuración y provisión de servicios REST mediante Retrofit.
 *
 * Este objeto singleton configura la conexión HTTP base, los interceptores de registro (logging),
 * y crea las instancias de las interfaces de servicio necesarias para la comunicación con el backend.
 */
object RetrofitClient {
    /**
     * URL base del servidor backend alojado en Render.
     */
    private const val BASE_URL = "https://ecovinedos-1.onrender.com"

    /**
     * Interceptor para registrar el cuerpo de las peticiones y respuestas HTTP en Logcat.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente OkHttp configurado con interceptores de red.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Instancia principal de Retrofit encargada de la serialización y deserialización (Gson).
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Servicio para la gestión de parcelas y sensores IoT. */
    val parcelaService: ParcelaService by lazy { retrofit.create(ParcelaService::class.java) }
    /** Servicio para la gestión de usuarios, perfiles y autenticación. */
    val usuarioService: UsuarioService by lazy { retrofit.create(UsuarioService::class.java) }
    /** Servicio para el registro de acciones y eventos en bitácora. */
    val bitacoraService: BitacoraService by lazy { retrofit.create(BitacoraService::class.java) }
    /** Servicio para el control y programación de sistemas de riego. */
    val riegoService: RiegoService by lazy { retrofit.create(RiegoService::class.java) }
    /** Servicio para el registro de muestras analíticas (Brix, pH, etc.). */
    val muestraService: MuestraService by lazy { retrofit.create(MuestraService::class.java) }
    /** Servicio para consultar el historial de mediciones de sensores. */
    val historialService: HistorialService by lazy { retrofit.create(HistorialService::class.java) }
    /** Servicio para la gestión de notificaciones push y alertas del sistema. */
    val notificacionService: NotificacionService by lazy { retrofit.create(NotificacionService::class.java) }
    /** Servicio para la gestión de eventos de turismo y actividades. */
    val eventoService: EventoService by lazy { retrofit.create(EventoService::class.java) }
    /** Servicio para la sincronización y vinculación con Android TV. */
    val tvService: TvService by lazy { retrofit.create(TvService::class.java) }
    /** Servicio para el monitoreo y gestión de condiciones en cava/bodega. */
    val cavaService: CavaService by lazy { retrofit.create(CavaService::class.java) }
    /** Servicio para la carga de archivos multimedia al servidor. */
    val uploadService: UploadService by lazy { retrofit.create(UploadService::class.java) }
}

