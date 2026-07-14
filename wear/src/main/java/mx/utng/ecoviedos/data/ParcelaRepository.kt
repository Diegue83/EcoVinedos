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
