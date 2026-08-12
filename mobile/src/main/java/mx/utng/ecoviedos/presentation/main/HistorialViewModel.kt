package mx.utng.ecoviedos.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.data.remote.RiegoResponse
import mx.utng.ecoviedos.data.repository.HistorialRepository
import mx.utng.ecoviedos.data.repository.RiegoRemoteRepository

/**
 * Estados de la pantalla de consulta histórica.
 */
sealed class HistorialUiState {
    data object Idle : HistorialUiState()
    data object Loading : HistorialUiState()
    data class Success(
        val historial: List<HistorialSensorResponse>,
        val resumen: List<ResumenDiarioResponse>,
        val riegos: List<RiegoResponse>
    ) : HistorialUiState()
    data class Error(val mensaje: String) : HistorialUiState()
}

/**
 * ViewModel encargado de la consulta de datos históricos de telemetría.
 */
class HistorialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistorialRepository()
    private val riegoRepository = RiegoRemoteRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Idle)
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _selectedParcelId = MutableStateFlow<String?>(null)
    val selectedParcelId: StateFlow<String?> = _selectedParcelId.asStateFlow()

    fun seleccionarParcela(id: String) {
        _selectedParcelId.value = id
        cargarDatos(id)
    }

    /**
     * Carga tanto el historial reciente como los resúmenes diarios de una parcela.
     * 
     * @param parcelaId Identificador de la parcela a consultar.
     */
    fun cargarDatos(parcelaId: String) {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Loading
            
            val token = sessionManager.token.first() ?: ""
            val histResult = repository.obtenerHistorial(parcelaId)
            val resResult = repository.obtenerResumen(parcelaId)
            val riegoResult = riegoRepository.obtenerRiegos(token, parcelaId, null)

            if (histResult.isSuccess && resResult.isSuccess) {
                _uiState.value = HistorialUiState.Success(
                    historial = histResult.getOrDefault(emptyList()),
                    resumen = resResult.getOrDefault(emptyList()),
                    riegos = riegoResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = HistorialUiState.Error("Error al cargar datos históricos")
            }
        }
    }
}
