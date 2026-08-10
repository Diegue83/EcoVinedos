package mx.utng.ecoviedos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://ecovinedos.onrender.com/"

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

    val parcelaService: ParcelaService by lazy { retrofit.create(ParcelaService::class.java) }
    val usuarioService: UsuarioService by lazy { retrofit.create(UsuarioService::class.java) }
    val bitacoraService: BitacoraService by lazy { retrofit.create(BitacoraService::class.java) }
    val riegoService: RiegoService by lazy { retrofit.create(RiegoService::class.java) }
    val muestraService: MuestraService by lazy { retrofit.create(MuestraService::class.java) }
    val historialService: HistorialService by lazy { retrofit.create(HistorialService::class.java) }
    val notificacionService: NotificacionService by lazy { retrofit.create(NotificacionService::class.java) }
}
