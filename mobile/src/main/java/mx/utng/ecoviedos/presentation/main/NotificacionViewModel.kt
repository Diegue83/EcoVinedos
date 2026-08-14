package mx.utng.ecoviedos.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.NotificacionResponse
import mx.utng.ecoviedos.data.repository.NotificacionRepository

/**
 * Estados de la pantalla de notificaciones.
 */
sealed class NotificacionUiState {
    data object Loading : NotificacionUiState()
    data class Success(val notificaciones: List<NotificacionResponse>) : NotificacionUiState()
    data class Error(val mensaje: String) : NotificacionUiState()
}

/**
 * ViewModel encargado de gestionar las notificaciones y el contador de no leídas.
 */
class NotificacionViewModel : ViewModel() {
    private val repository = NotificacionRepository()

    private val _uiState = MutableStateFlow<NotificacionUiState>(NotificacionUiState.Loading)
    val uiState: StateFlow<NotificacionUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    /**
     * Obtiene el listado de notificaciones del servidor y actualiza el contador.
     */
    fun cargarNotificaciones(token: String) {
        viewModelScope.launch {
            _uiState.value = NotificacionUiState.Loading
            repository.obtenerMisNotificaciones(token)
                .onSuccess { list ->
                    _uiState.value = NotificacionUiState.Success(list)
                    _unreadCount.value = list.count { it.estado == "no leida" }
                }
                .onFailure {
                    _uiState.value = NotificacionUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    /**
     * Cambia el estado de una notificación.
     */
    fun cambiarEstado(token: String, id: String, nuevoEstado: String) {
        viewModelScope.launch {
            repository.cambiarEstado(token, id, nuevoEstado)
                .onSuccess { cargarNotificaciones(token) }
        }
    }
}
