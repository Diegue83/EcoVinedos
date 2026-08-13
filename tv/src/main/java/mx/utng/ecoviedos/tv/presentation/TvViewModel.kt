package mx.utng.ecoviedos.tv.presentation

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.PairCodeRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient

sealed class TvUiState {
    data object Loading : TvUiState()
    data class NotLinked(val pairingCode: String) : TvUiState()
    data object Linked : TvUiState()
    data class Error(val message: String) : TvUiState()
}

class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvUiState>(TvUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val deviceId: String = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "tv_emulator_id"

    init {
        checkStatusAndStartPairing()
    }

    private fun checkStatusAndStartPairing() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.tvService.checkStatus(deviceId)
                    if (response.isSuccessful) {
                        val session = response.body()
                        if (session?.isLinked == true) {
                            _uiState.value = TvUiState.Linked
                            break // Salir del bucle al estar vinculado
                        } else if (session != null) {
                            // Si existe la sesión pero no está vinculada, mostramos el código actual
                            _uiState.value = TvUiState.NotLinked(session.pairingCode)
                        }
                    } else {
                        // Si no hay sesión (404), pedir una nueva
                        getNewPairingCode()
                    }
                } catch (e: Exception) {
                    // No cambiar el estado a Error aquí para no interrumpir el flujo visual si es solo un fallo de red temporal
                }
                delay(5000) // Revisar cada 5 segundos es suficiente
            }
        }
    }

    private suspend fun getNewPairingCode() {
        try {
            val response = RetrofitClient.tvService.getPairingCode(PairCodeRequest(deviceId))
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = TvUiState.NotLinked(response.body()!!.pairingCode)
            }
        } catch (e: Exception) {
            _uiState.value = TvUiState.Error("Error al obtener código")
        }
    }
}
