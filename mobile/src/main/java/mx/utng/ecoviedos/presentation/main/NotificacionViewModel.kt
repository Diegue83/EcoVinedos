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
    /** Lista de alertas recibidas. */
    data class Success(val notificaciones: List<NotificacionResponse>) : NotificacionUiState()
    data class Error(val mensaje: String) : NotificacionUiState()
}

/**
 * ViewModel encargado de gestionar las notificaciones y el contador de mensajes sin leer.
 */
class NotificacionViewModel : ViewModel() {
    private val repository = NotificacionRepository()

    private val _uiState = MutableStateFlow<NotificacionUiState>(NotificacionUiState.Loading)
    /** Flujo de estado de las notificaciones. */
    val uiState: StateFlow<NotificacionUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    /** Cantidad actual de mensajes marcados como "no leida". */
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    /**
     * Obtiene el listado de notificaciones del servidor y actualiza el contador global.
     *
     * @param token Token JWT del usuario.
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
     * Modifica el estado de una alerta (ej. marcar como leída).
     *
     * @param token Token JWT.
     * @param id Identificador de la notificación.
     * @param nuevoEstado Estado a establecer (leida/no leida).
     */
    fun cambiarEstado(token: String, id: String, nuevoEstado: String) {
        viewModelScope.launch {
            repository.cambiarEstado(token, id, nuevoEstado)
                .onSuccess { cargarNotificaciones(token) }
        }
    }
}
