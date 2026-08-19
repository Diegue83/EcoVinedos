package mx.utng.ecoviedos.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

/**
 * Gestor de persistencia local y caché para el módulo Wear OS.
 *
 * Almacena la lista de parcelas en SharedPreferences para permitir una carga instantánea
 * al abrir la aplicación sin depender de una sincronización inmediata con el móvil o MQTT.
 */
object ParcelaRepository {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    /** Flujo reactivo con la lista actual de parcelas cargadas en memoria. */
    val parcelas: StateFlow<List<Parcela>> = _parcelas

    /**
     * Inicializa el repositorio cargando los datos persistidos en la caché local.
     *
     * @param context Contexto necesario para acceder a SharedPreferences.
     */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
        val json = prefs.getString("parcelas_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Parcela>>() {}.type
                _parcelas.value = Gson().fromJson(json, type)
            } catch (e: Exception) {}
        }
    }

    /**
     * Actualiza la lista de parcelas en memoria y opcionalmente en el almacenamiento persistente.
     *
     * @param newList Nueva lista de parcelas a almacenar.
     * @param context Contexto para persistir los cambios (opcional).
     */
    fun updateParcelas(newList: List<Parcela>, context: Context? = null) {
        _parcelas.value = newList
        context?.let {
            val prefs = it.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
            prefs.edit().putString("parcelas_list", Gson().toJson(newList)).apply()
        }
    }
}

/**
 * Clase de utilidad para el mapeo de datos entre el mensaje JSON y el modelo de dominio.
 */
data class ParcelaMap(
    val _id: String,
    val nombreParcela: String?,
    val variedad: String?,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val umbralHumedadSuelo: Float? = null,
    val indiceMaduracion: Float,
    val fechaCosecha: Date?,
    val activa: Boolean,
    val humedad: Float,
    val temperatura: Float,
    val humedadSuelo: Float? = null,
    val riegoActivo: Boolean? = null,
    val tiempoRestanteRiego: Int? = null,
    val tipoRiego: String? = null,
    val nodoVinculado: String? = null
)
