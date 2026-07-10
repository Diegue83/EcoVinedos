package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.WearableDataSender
import mx.utng.ecoviedos.data.api.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas.asStateFlow()
    
    private val wearableDataSender = WearableDataSender(application)

    private val mockData = listOf(
        Parcela("4", "Merlot", "Variedad 1", 1000, 30f, 25f, 0.74f, Date(), true, 42f, 22f),
        Parcela("7", "Cabernet", "Variedad 2", 1500, 30f, 25f, 0.65f, Date(), true, 22f, 26f),
        Parcela("9", "Syrah", "Variedad 3", 1200, 30f, 25f, 0.68f, Date(), true, 65f, 20f)
    )

    init {
        loadParcelas()
    }

    fun reloadParcelas() {
        loadParcelas()
    }

    private fun loadParcelas() {
        viewModelScope.launch {
            try {
                val realData = RetrofitClient.apiService.getParcelas()
                if (realData.isNotEmpty()) {
                    _parcelas.value = realData
                    wearableDataSender.sendParcelas(realData)
                } else { useFallbackData() }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Servidor no disponible, enviando mocks al reloj")
                useFallbackData()
            }
        }
    }

    private fun useFallbackData() {
        _parcelas.value = mockData
        wearableDataSender.sendParcelas(mockData)
    }
}
