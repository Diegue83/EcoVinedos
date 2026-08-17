package mx.utng.ecoviedos.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.repository.EventoRepository

class TourismViewModel : ViewModel() {
    private val repository = EventoRepository()

    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarEventos()
    }

    fun cargarEventos(tipo: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.obtenerEventos(tipo).onSuccess {
                _eventos.value = it
            }
            _isLoading.value = false
        }
    }

    fun crearEvento(token: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.crearEvento(token, request).onSuccess {
                cargarEventos()
                onExito()
            }
            _isLoading.value = false
        }
    }

    fun actualizarEvento(token: String, id: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.actualizarEvento(token, id, request).onSuccess {
                cargarEventos()
                onExito()
            }
            _isLoading.value = false
        }
    }

    fun eliminarEvento(token: String, id: String) {
        viewModelScope.launch {
            repository.eliminarEvento(token, id).onSuccess {
                cargarEventos()
            }
        }
    }
}
