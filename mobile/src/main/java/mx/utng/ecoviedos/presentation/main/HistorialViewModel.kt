package mx.utng.ecoviedos.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.repository.HistorialRepository

sealed class HistorialUiState {
    data object Idle : HistorialUiState()
    data object Loading : HistorialUiState()
    data class Success(
        val historial: List<HistorialSensorResponse>,
        val resumen: List<ResumenDiarioResponse>
    ) : HistorialUiState()
    data class Error(val mensaje: String) : HistorialUiState()
}

class HistorialViewModel : ViewModel() {
    private val repository = HistorialRepository()

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Idle)
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    fun cargarDatos(parcelaId: String) {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Loading
            
            val histResult = repository.obtenerHistorial(parcelaId)
            val resResult = repository.obtenerResumen(parcelaId)

            if (histResult.isSuccess && resResult.isSuccess) {
                _uiState.value = HistorialUiState.Success(
                    historial = histResult.getOrDefault(emptyList()),
                    resumen = resResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = HistorialUiState.Error("Error al cargar datos históricos")
            }
        }
    }
}
