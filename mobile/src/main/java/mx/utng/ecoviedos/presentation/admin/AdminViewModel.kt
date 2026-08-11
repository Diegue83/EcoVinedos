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
import mx.utng.ecoviedos.data.remote.UsuarioRequest
import mx.utng.ecoviedos.data.remote.UsuarioResponse
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.data.repository.UsuarioRepository
import mx.utng.ecoviedos.data.repository.NotificacionRepository
import mx.utng.ecoviedos.presentation.main.MainViewModel

sealed class AddParcelUiState {
    data object Idle : AddParcelUiState()
    data object Loading : AddParcelUiState()
    data object Success : AddParcelUiState()
    data class Error(val mensaje: String) : AddParcelUiState()
}

sealed class UserManagementUiState {
    data object Idle : UserManagementUiState()
    data object Loading : UserManagementUiState()
    data class Success(val users: List<UsuarioResponse>) : UserManagementUiState()
    data class Error(val mensaje: String) : UserManagementUiState()
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val parcelaRepository = ParcelaRepository()
    private val usuarioRepository = UsuarioRepository()
    private val notificacionRepository = NotificacionRepository()

    private var mainViewModel: MainViewModel? = null

    private val _uiState = MutableStateFlow<AddParcelUiState>(AddParcelUiState.Idle)
    val uiState: StateFlow<AddParcelUiState> = _uiState.asStateFlow()

    private val _userUiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Idle)
    val userUiState: StateFlow<UserManagementUiState> = _userUiState.asStateFlow()

    fun setMainViewModel(viewModel: MainViewModel) {
        mainViewModel = viewModel
    }

    fun addParcel(
        nombre: String,
        variedad: String,
        area: Int,
        umbralHumedad: Float,
        umbralTemp: Float,
        umbralHumedadSuelo: Float,
        humedadOptimaSuelo: Float,
        consumoAguaM2: Float,
        tipoRiego: String
    ) {
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
                umbralTemp = umbralTemp.toDouble(),
                umbralHumedadSuelo = umbralHumedadSuelo.toDouble(),
                humedadOptimaSuelo = humedadOptimaSuelo.toDouble(),
                consumoAguaM2 = consumoAguaM2.toDouble(),
                tipoRiego = tipoRiego
            )

            val resultado = parcelaRepository.crearParcela(token, request)
            resultado
                .onSuccess { parcela ->
                    mainViewModel?.cargarParcelas()
                    _uiState.value = AddParcelUiState.Success
                    
                    // Notificar sobre la nueva parcela
                    val mensajeNotif = if (parcela.nodoVinculado == null) {
                        "Nueva parcela '${parcela.nombreParcela}' registrada. Aún no tiene un nodo IoT vinculado."
                    } else {
                        "Nueva parcela '${parcela.nombreParcela}' registrada y vinculada."
                    }
                    
                    notificacionRepository.crearNotificacion(
                        tipo = "sistema",
                        titulo = "Parcela Registrada",
                        mensaje = mensajeNotif,
                        parcelaId = parcela.id
                    )
                }
                .onFailure { e ->
                    _uiState.value = AddParcelUiState.Error(e.message ?: "Error al guardar")
                }
        }
    }

    fun updateParcel(
        id: String,
        nombre: String,
        variedad: String,
        area: Int,
        umbralHumedad: Float,
        umbralTemp: Float,
        umbralHumedadSuelo: Float,
        humedadOptimaSuelo: Float,
        consumoAguaM2: Float,
        activa: Boolean,
        tipoRiego: String
    ) {
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
                umbralTemp = umbralTemp.toDouble(),
                umbralHumedadSuelo = umbralHumedadSuelo.toDouble(),
                humedadOptimaSuelo = humedadOptimaSuelo.toDouble(),
                consumoAguaM2 = consumoAguaM2.toDouble(),
                tipoRiego = tipoRiego
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

    // --- User Management ---

    fun loadUsers() {
        viewModelScope.launch {
            _userUiState.value = UserManagementUiState.Loading
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) {
                _userUiState.value = UserManagementUiState.Error("No hay sesión activa")
                return@launch
            }

            usuarioRepository.obtenerUsuarios(token)
                .onSuccess { users ->
                    _userUiState.value = UserManagementUiState.Success(users)
                }
                .onFailure { e ->
                    _userUiState.value = UserManagementUiState.Error(e.message ?: "Fallo al cargar usuarios")
                }
        }
    }

    fun createUser(nombre: String, correo: String, contrasena: String, rol: String, telefono: String?) {
        viewModelScope.launch {
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) return@launch

            val request = UsuarioRequest(nombre, correo, contrasena, rol, telefono)
            usuarioRepository.crearUsuario(token, request)
                .onSuccess { loadUsers() }
        }
    }

    fun updateUser(id: String, nombre: String, correo: String, rol: String, telefono: String?) {
        viewModelScope.launch {
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) return@launch

            val request = UsuarioRequest(nombre, correo, null, rol, telefono)
            usuarioRepository.actualizarUsuario(token, id, request)
                .onSuccess { loadUsers() }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            val token = sessionManager.token.first()
            if (token.isNullOrBlank()) return@launch

            usuarioRepository.eliminarUsuario(token, id)
                .onSuccess { loadUsers() }
        }
    }
}