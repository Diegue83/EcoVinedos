package mx.utng.ecoviedos.presentation.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.remote.ParcelaRequest
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.presentation.main.MainViewModel

sealed class AddParcelUiState {
    data object Idle : AddParcelUiState()
    data object Loading : AddParcelUiState()
    data object Success : AddParcelUiState()
    data class Error(val mensaje: String) : AddParcelUiState()
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val parcelaRepository = ParcelaRepository()

    private var mainViewModel: MainViewModel? = null

    private val _uiState = MutableStateFlow<AddParcelUiState>(AddParcelUiState.Idle)
    val uiState: StateFlow<AddParcelUiState> = _uiState.asStateFlow()

    fun setMainViewModel(viewModel: MainViewModel) {
        mainViewModel = viewModel
    }

    fun addParcel(nombre: String, variedad: String, area: Int, umbralHumedad: Float, umbralTemp: Float) {
        viewModelScope.launch {
            _uiState.value = AddParcelUiState.Loading

            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AddParcelUiState.Error("No hay sesión activa")
                return@launch
            }

            val request = ParcelaRequest(
                nombreParcela = nombre,
                areaM2 = area.toDouble(),
                variedad = variedad,
                activa = true,
                umbralHumedad = umbralHumedad.toDouble(),
                umbralTemp = umbralTemp.toDouble()
            )

            val resultado = parcelaRepository.crearParcela(token, request)
            resultado
                .onSuccess {
                    mainViewModel?.cargarParcelas()
                    _uiState.value = AddParcelUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = AddParcelUiState.Error(e.message ?: "Error al guardar")
                }
        }
    }

    fun updateParcel(id: String, nombre: String, variedad: String, area: Int, umbralHumedad: Float, umbralTemp: Float, activa: Boolean) {
        viewModelScope.launch {
            _uiState.value = AddParcelUiState.Loading

            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AddParcelUiState.Error("No hay sesión activa")
                return@launch
            }

            val request = ParcelaRequest(
                nombreParcela = nombre,
                areaM2 = area.toDouble(),
                variedad = variedad,
                activa = activa,
                umbralHumedad = umbralHumedad.toDouble(),
                umbralTemp = umbralTemp.toDouble()
            )

            val resultado = parcelaRepository.actualizarParcela(token, id, request)
            resultado
                .onSuccess {
                    mainViewModel?.cargarParcelas()
                    _uiState.value = AddParcelUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = AddParcelUiState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun deleteParcel(id: String) {
        viewModelScope.launch {
            _uiState.value = AddParcelUiState.Loading

            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _uiState.value = AddParcelUiState.Error("No hay sesión activa")
                return@launch
            }

            val resultado = parcelaRepository.eliminarParcela(token, id)
            resultado
                .onSuccess {
                    mainViewModel?.cargarParcelas()
                    _uiState.value = AddParcelUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = AddParcelUiState.Error(e.message ?: "Error al eliminar")
                }
        }
    }

    fun resetState() {
        _uiState.value = AddParcelUiState.Idle
    }

    fun addUser(nombre: String, correo: String, rol: String) {
        // Lógica de usuario (pendiente, siguiente paso)
    }
}