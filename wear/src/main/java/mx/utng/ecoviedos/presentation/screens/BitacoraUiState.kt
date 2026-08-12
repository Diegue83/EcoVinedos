package mx.utng.ecoviedos.presentation.screens

import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.model.Parcela
import java.io.File

data class BitacoraUiState(
    val parcelas: List<Parcela> = emptyList(),
    val bitacoras: List<File> = emptyList(),
    val recordedTime: String = "00:00",
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val lastAudioPath: String? = null,
    val isIrrigationActive: Boolean = false,
    val connectionMessage: String = "Conectando..."
)
