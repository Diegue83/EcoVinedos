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

    init {
        cargarNotificaciones()
    }

    /**
     * Obtiene el listado de notificaciones del servidor y actualiza el contador.
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.value = NotificacionUiState.Loading
            repository.obtenerNotificaciones()
                .onSuccess { list ->
                    _uiState.value = NotificacionUiState.Success(list)
                    _unreadCount.value = list.count { !it.leida }
                }
                .onFailure {
                    _uiState.value = NotificacionUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    /**
     * Marca una notificación como leída en el servidor.
     * 
     * @param id Identificador de la notificación.
     */
    fun marcarComoLeida(id: String) {
        viewModelScope.launch {
            repository.marcarLeida(id)
                .onSuccess { cargarNotificaciones() }
        }
    }

    /**
     * Elimina del servidor todas las notificaciones marcadas como leídas.
     */
    fun limpiarLeidas() {
        viewModelScope.launch {
            repository.limpiarLeidas()
                .onSuccess { cargarNotificaciones() }
        }
    }
}
