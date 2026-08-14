package mx.utng.ecoviedos.presentation.enologo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.*
import mx.utng.ecoviedos.data.repository.EventoRepository

/**
 * ViewModel para el perfil de Enólogo.
 * Gestiona la carga de datos de cavas, secciones y eventos de turismo.
 */
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

    /**
     * Carga todos los datos necesarios para el dashboard del enólogo.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar Eventos de turismo/actividades
                eventoRepository.obtenerEventos().onSuccess {
                    _eventos.value = it
                }
                
                // Cargar Estructura de Cavas y Secciones
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful) {
                    _cavas.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Manejo de errores silencioso por ahora
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Gestión de Cavas ---

    fun crearCava(token: String, nombre: String, ubicacion: String, descripcion: String?) {
        viewModelScope.launch {
            try {
                RetrofitClient.cavaService.crearCava("Bearer $token", CavaRequest(nombre, ubicacion, descripcion))
                cargarDatos()
            } catch (e: Exception) {}
        }
    }

    fun eliminarCava(token: String, id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.cavaService.eliminarCava("Bearer $token", id)
                cargarDatos()
            } catch (e: Exception) {}
        }
    }

    // --- Gestión de Secciones ---

    fun crearSeccion(token: String, cavaId: String, nombre: String, tipo: String, capacidad: Int) {
        viewModelScope.launch {
            try {
                val request = SeccionCavaRequest(cava = cavaId, nombre = nombre, tipo = tipo, capacidadBotellas = capacidad)
                RetrofitClient.cavaService.crearSeccion("Bearer $token", request)
                cargarDatos()
            } catch (e: Exception) {}
        }
    }

    fun actualizarBotellas(token: String, seccionId: String, cantidad: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.cavaService.actualizarSeccion(
                    "Bearer $token", 
                    seccionId, 
                    mapOf("botellasActuales" to cantidad)
                )
                cargarDatos()
            } catch (e: Exception) {}
        }
    }

    fun eliminarSeccion(token: String, id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.cavaService.eliminarSeccion("Bearer $token", id)
                cargarDatos()
            } catch (e: Exception) {}
        }
    }

    // --- Gestión de Eventos ---

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
}
