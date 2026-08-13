package mx.utng.ecoviedos.presentation.enologo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.repository.EventoRepository
// Assume CavaRepository exists or will be created
// import mx.utng.ecoviedos.data.repository.CavaRepository

class EnologoViewModel : ViewModel() {
    private val eventoRepository = EventoRepository()
    
    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _isLoading.value = true
            eventoRepository.obtenerEventos().onSuccess {
                _eventos.value = it
            }
            _isLoading.value = false
        }
    }

    fun registrarEvento(token: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            eventoRepository.crearEvento(token, request).onSuccess {
                cargarEventos()
                onExito()
            }
            _isLoading.value = false
        }
    }
}
