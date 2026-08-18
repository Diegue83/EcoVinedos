package mx.utng.ecoviedos.presentation.enologo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.shared.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.remote.*
import mx.utng.ecoviedos.data.repository.EventoRepository

/**
 * ViewModel para el perfil de Enólogo.
 * Gestiona la carga de datos de cavas, secciones y eventos de turismo.
 */
class EnologoViewModel(application: Application) : AndroidViewModel(application) {
    private val eventoRepository = EventoRepository()
    private var mqttManager: MqttManager? = null
    
    private val _eventos = MutableStateFlow<List<EventoResponse>>(emptyList())
    val eventos = _eventos.asStateFlow()

    private val _cavas = MutableStateFlow<List<CavaResponse>>(emptyList())
    val cavas = _cavas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarDatos()
        initializeMqtt()
    }

    private fun initializeMqtt() {
        mqttManager = MqttManager(
            context = getApplication(),
            onMessageReceived = { id, hum, temp, _, _, _ ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarSeccionEnTiempoReal(id, hum, temp)
                }
            },
            onRiegoStatusReceived = { _, _, _ -> },
            onParcelListReceived = { },
            onCavaListReceived = { payload ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarListaCavasMqtt(payload)
                }
            },
            onConnectionStatusChanged = { _, _ -> }
        )
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
        }
    }

    private fun actualizarSeccionEnTiempoReal(id: String, hum: Float, temp: Float) {
        val currentCavas = _cavas.value.toMutableList()
        var changed = false
        
        val updatedCavas = currentCavas.map { cava ->
            val index = cava.secciones.indexOfFirst { it._id == id }
            if (index != -1) {
                changed = true
                val updatedSecciones = cava.secciones.toMutableList()
                updatedSecciones[index] = updatedSecciones[index].copy(
                    humedad = hum.toDouble(),
                    temperatura = temp.toDouble(),
                    ultimaLectura = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                )
                cava.copy(secciones = updatedSecciones)
            } else {
                cava
            }
        }
        
        if (changed) {
            _cavas.value = updatedCavas
        }
    }

    private fun actualizarListaCavasMqtt(payload: String) {
        try {
            val type = object : TypeToken<List<SeccionCavaResponse>>() {}.type
            val list = Gson().fromJson<List<SeccionCavaResponse>>(payload, type)
            
            // Aquí agrupamos las secciones de vuelta en sus cavas correspondientes
            // O si el payload ya viniera agrupado sería más fácil, pero con la lógica actual de connecction.js
            // vinedo/secciones/lista envía un array plano de SeccionCavaResponse
            
            val currentCavas = _cavas.value.toMutableList()
            val updatedCavas = currentCavas.map { cava ->
                val seccionesActualizadas = cava.secciones.map { seccion ->
                    list.find { it._id == seccion._id } ?: seccion
                }
                cava.copy(secciones = seccionesActualizadas)
            }
            _cavas.value = updatedCavas
        } catch (e: Exception) {
            Log.e("EnologoViewModel", "Error parseando lista cavas MQTT", e)
        }
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
                Log.e("EnologoViewModel", "Error cargando datos", e)
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

    fun actualizarBotellas(token: String, seccionId: String, cantidad: Int, onComplete: () -> Unit = {}) {
        if (token.isBlank()) {
            Log.e("EnologoViewModel", "Error: Token vacío al intentar actualizar botellas")
            onComplete()
            return
        }
        
        viewModelScope.launch {
            try {
                // Buscar la sección actual para enviar los datos requeridos por el backend
                val seccionActual = _cavas.value.flatMap { it.secciones }.find { it._id == seccionId }
                
                val request = SeccionCavaRequest(
                    botellasActuales = cantidad,
                    nombre = seccionActual?.nombre,
                    tipo = seccionActual?.tipo,
                    capacidadBotellas = seccionActual?.capacidadBotellas,
                    cava = seccionActual?.cava
                )

                Log.d("EnologoViewModel", "Enviando PUT para sección $seccionId: $request")

                val response = RetrofitClient.cavaService.actualizarSeccion(
                    "Bearer $token", 
                    seccionId, 
                    request,
                )
                if (response.isSuccessful) {
                    Log.d("EnologoViewModel", "Botellas actualizadas exitosamente: $cantidad")
                    cargarDatos()
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("EnologoViewModel", "Error al actualizar botellas: ${response.code()} - $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("EnologoViewModel", "Excepción al actualizar botellas", e)
            } finally {
                onComplete()
            }
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

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
    }
}
