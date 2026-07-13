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

    // Fallback data
    init {
        _parcelas.value = listOf(
            Parcela("4", "Merlot (Default)", "Variedad 1", 1000, 38f, 1.0f, 22f, Date(), true, 30f, 20f),
            Parcela("5", "Cabernet (Default)", "Variedad 2", 1500, 22f, 1.2f, 24f, Date(), true, 35f, 25f),
            Parcela("6", "Syrah (Default)", "Variedad 3", 1200, 65f, 0.9f, 21f, Date(), true, 40f, 22f)
        )
    }
}
