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

sealed class MuestraUiState {
    data object Idle : MuestraUiState()
    data object Loading : MuestraUiState()
    data class Success(val historial: List<MuestraResponse>) : MuestraUiState()
    data class Error(val mensaje: String) : MuestraUiState()
}

class MuestraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MuestraRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<MuestraUiState>(MuestraUiState.Idle)
    val uiState: StateFlow<MuestraUiState> = _uiState.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

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

    fun registrarMuestra(
        parcelaId: String,
        brix: Double,
        ph: Double,
        acidez: Double,
        phSuelo: Double,
        observaciones: String
    ) {
        viewModelScope.launch {
            _uiState.value = MuestraUiState.Loading
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) return@launch

            val request = MuestraRequest(parcelaId, brix, ph, acidez, phSuelo, observaciones)
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
    
    fun resetRegistroState() {
        _registroExitoso.value = false
    }
}
