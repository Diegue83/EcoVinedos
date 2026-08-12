package mx.utng.ecoviedos.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

object ParcelaRepository {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas

    fun updateParcelas(newList: List<Parcela>) {
        _parcelas.value = newList
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
