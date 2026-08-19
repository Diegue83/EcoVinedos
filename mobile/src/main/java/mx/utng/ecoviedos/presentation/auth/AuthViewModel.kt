package mx.utng.ecoviedos.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.remote.LoginRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Estados de la interfaz de autenticación.
 */
sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class LoginSuccess(val rol: String) : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
    data object CodeSent : AuthUiState()
    data object CodeVerified : AuthUiState()
}

/**
 * ViewModel encargado del flujo de autenticación y recuperación de contraseñas.
 *
 * @param application Instancia de la aplicación.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    /** Flujo de estado de la UI de autenticación. */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     *
     * @param correo Email del usuario.
     * @param contraseña Password del usuario.
     */
    fun login(correo: String, contraseña: String) {
        if (correo.isBlank() || contraseña.isBlank()) {
            _uiState.value = AuthUiState.Error("Completa correo y contraseña")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuarioService.login(LoginRequest(correo, contraseña))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.guardarSesion(body.token, body._id, body.nombre, body.rol)
                    _uiState.value = AuthUiState.LoginSuccess(body.rol)
                } else {
                    _uiState.value = AuthUiState.Error("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión: revisa tu red o el servidor")
            }
        }
    }

    /**
     * Solicita un código de recuperación de contraseña al correo electrónico.
     *
     * @param correo Email del usuario.
     */
    fun solicitarCodigo(correo: String) {
        if (correo.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu correo")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuarioService.solicitarRecuperacion(mapOf("correo" to correo))
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.CodeSent
                } else {
                    _uiState.value = AuthUiState.Error("No se pudo enviar el código. Verifica el correo.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión")
            }
        }
    }

    /**
     * Verifica si el código ingresado es válido para la recuperación.
     *
     * @param correo Email del usuario.
     * @param codigo Código de 6 dígitos recibido.
     */
    fun verificarCodigo(correo: String, codigo: String) {
        if (codigo.length != 6) {
            _uiState.value = AuthUiState.Error("El código debe ser de 6 dígitos")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuarioService.verificarCodigo(mapOf("correo" to correo, "codigo" to codigo))
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.CodeVerified
                } else {
                    _uiState.value = AuthUiState.Error("Código incorrecto o expirado")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión")
            }
        }
    }

    /**
     * Establece una nueva contraseña tras validar el código.
     *
     * @param correo Email del usuario.
     * @param codigo Código verificado.
     * @param nuevaPass Nueva contraseña a establecer.
     */
    fun restablecerContraseña(correo: String, codigo: String, nuevaPass: String) {
        if (nuevaPass.length < 6) {
            _uiState.value = AuthUiState.Error("Mínimo 6 caracteres")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuarioService.reestablecerContraseña(
                    mapOf("correo" to correo, "codigo" to codigo, "nuevaContraseña" to nuevaPass)
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("No se pudo restablecer la contraseña")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión")
            }
        }
    }

    /**
     * Regresa el estado del flujo a su valor inicial.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
