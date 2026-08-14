package mx.utng.ecoviedos.presentation.enologo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.CavaResponse
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.repository.EventoRepository

class EnologoViewModel : ViewModel() {
    private val eventoRepository = EventoRepository()
    
    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()

    private val _cavas = MutableStateFlow<List<CavaResponse>>(emptyList())
    val cavas = _cavas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar Eventos
                eventoRepository.obtenerEventos().onSuccess {
                    _eventos.value = it
                }
                
                // Cargar Cavas
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful) {
                    _cavas.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registrarEvento(token: String, request: EventoRequest, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            eventoRepository.crearEvento(token, request).onSuccess {
                cargarDatos()
                onExito()
            }
            _isLoading.value = false
        }
    }

    fun actualizarBotellas(token: String, id: String, cantidad: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.cavaService.actualizarBotellas("Bearer $token", id, mapOf("botellasActuales" to cantidad))
                cargarDatos()
            } catch (e: Exception) {}
        }
    }
}
