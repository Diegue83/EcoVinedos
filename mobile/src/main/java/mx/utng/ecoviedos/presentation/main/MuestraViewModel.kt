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
import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.repository.MuestraRepository

/**
 * Estados posibles de la interfaz de muestras.
 */
sealed class MuestraUiState {
    data object Idle : MuestraUiState()
    data object Loading : MuestraUiState()
    /** Contiene la lista histórica de análisis para la parcela. */
    data class Success(val historial: List<MuestraResponse>) : MuestraUiState()
    data class Error(val mensaje: String) : MuestraUiState()
}

/**
 * ViewModel encargado de la gestión de muestras analíticas (laboratorio de campo).
 *
 * @param application Instancia de la aplicación.
 */
class MuestraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MuestraRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<MuestraUiState>(MuestraUiState.Idle)
    /** Flujo de estado de la consulta de muestras. */
    val uiState: StateFlow<MuestraUiState> = _uiState.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    /** Notifica si la última muestra se guardó correctamente. */
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

    /**
     * Carga el historial de muestras para una parcela determinada.
     * 
     * @param parcelaId Identificador único de la parcela.
     */
    fun cargarHistorial(parcelaId: String) {
        viewModelScope.launch {
            _uiState.value = MuestraUiState.Loading
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _uiState.value = MuestraUiState.Error("Sesión no válida")
                return@launch
            }

            repository.obtenerHistorial(token, parcelaId)
                .onSuccess {
                    _uiState.value = MuestraUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = MuestraUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    /**
     * Registra una nueva muestra de campo en el servidor.
     * 
     * @param parcelaId ID de la parcela vinculada.
     * @param brix Grados Brix medidos.
     * @param ph Nivel de pH medido.
     * @param acidez Nivel de acidez medido.
     * @param phSuelo pH del suelo.
     * @param indiceMaduracion Índice calculado de madurez.
     * @param observaciones Comentarios adicionales.
     */
    fun registrarMuestra(
        parcelaId: String,
        brix: Double,
        ph: Double,
        acidez: Double,
        phSuelo: Double,
        indiceMaduracion: Double?,
        observaciones: String
    ) {
        viewModelScope.launch {
            _uiState.value = MuestraUiState.Loading
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) return@launch

            val request = MuestraRequest(parcelaId, brix, ph, acidez, phSuelo, indiceMaduracion, observaciones)
            repository.registrarMuestra(token, request)
                .onSuccess {
                    _registroExitoso.value = true
                    cargarHistorial(parcelaId)
                }
                .onFailure {
                    _uiState.value = MuestraUiState.Error(it.message ?: "Fallo al registrar")
                }
        }
    }
    
    /**
     * Resetea el flag de éxito de registro.
     */
    fun resetRegistroState() {
        _registroExitoso.value = false
    }
}
