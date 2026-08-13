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
    data class Linked(val cavas: List<mx.utng.ecoviedos.data.remote.CavaResponse>) : TvUiState()
    data class Error(val message: String) : TvUiState()
}

class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvUiState>(TvUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val deviceId: String = android.provider.Settings.Secure.getString(
        application.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
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
                            cargarDatosCava()
                            // No paramos el bucle si queremos refrescar datos, pero para estado vinculado sí
                            break 
                        } else if (session != null) {
                            _uiState.value = TvUiState.NotLinked(session.pairingCode)
                        }
                    } else {
                        getNewPairingCode()
                    }
                } catch (e: Exception) { }
                delay(5000)
            }
        }
    }

    private fun cargarDatosCava() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TvUiState.Linked(response.body()!!)
                }
            } catch (e: Exception) {
                _uiState.value = TvUiState.Error("Error al cargar cavas")
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
