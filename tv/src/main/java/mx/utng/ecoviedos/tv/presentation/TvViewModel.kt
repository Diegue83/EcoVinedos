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
                    if (response.isSuccessful && response.body()?.isLinked == true) {
                        _uiState.value = TvUiState.Linked
                        // Once linked, we can stop the loop or transition to data fetching
                        break
                    } else {
                        // If not linked or session expired, get a new code
                        getNewPairingCode()
                    }
                } catch (e: Exception) {
                    _uiState.value = TvUiState.Error("Error de conexión")
                }
                delay(10000) // Polling every 10 seconds
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
