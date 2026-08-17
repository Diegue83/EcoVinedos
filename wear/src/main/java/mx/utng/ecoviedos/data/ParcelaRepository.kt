package mx.utng.ecoviedos.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

object ParcelaRepository {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas

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

    fun updateParcelas(newList: List<Parcela>, context: Context? = null) {
        _parcelas.value = newList
        context?.let {
            val prefs = it.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
            prefs.edit().putString("parcelas_list", Gson().toJson(newList)).apply()
        }
    }
}

data class ParcelaMap(
    val id: String,
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
    val tipoRiego: String? = null
)
