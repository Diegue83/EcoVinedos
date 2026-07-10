package mx.utng.ecoviedos.presentation.screens

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.data.ParcelaMap
import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.domain.usecase.GuardarBitacoraUseCase
import mx.utng.ecoviedos.domain.usecase.ObtenerBitacorasUseCase
import java.io.File
import java.util.Date

class BitacoraViewModel(
    application: Application,
    private val guardarBitacoraUseCase: GuardarBitacoraUseCase,
    private val obtenerBitacorasUseCase: ObtenerBitacorasUseCase
) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {

    private val _uiState = MutableStateFlow(BitacoraUiState())
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    private val _selectedParcelId = MutableStateFlow("4")
    val selectedParcelId: StateFlow<String> = _selectedParcelId.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null
    private var audioFile: File? = null
    private val gson = Gson()

    init {
        Wearable.getMessageClient(application).addListener(this)
        viewModelScope.launch {
            ParcelaRepository.parcelas.collect { parcelas ->
                _uiState.value = _uiState.value.copy(parcelas = parcelas)
                Log.d("BitacoraViewModel", "UI actualizada desde Repositorio: ${parcelas.size} parcelas")
            }
        }
        cargarBitacoras(_selectedParcelId.value)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/parcelas_message") {
            val json = String(messageEvent.data, Charsets.UTF_8)
            try {
                val itemType = object : TypeToken<List<ParcelaMap>>() {}.type
                val parcelasMobile: List<ParcelaMap> = gson.fromJson(json, itemType)
                val parcelasWear = parcelasMobile.map { m ->
                    Parcela(
                        id = m.id,
                        nombreParcela = m.nombreParcela ?: "Parcela ${m.id}",
                        variedad = m.variedad ?: "",
                        areaM2 = m.areaM2,
                        umbralHumedad = m.umbralHumedad,
                        umbralTemp = m.umbralTemp,
                        indiceMaduracion = m.indiceMaduracion,
                        fechaCosecha = m.fechaCosecha ?: Date(),
                        activa = m.activa,
                        humedad = m.humedad,
                        temperatura = m.temperatura
                    )
                }
                ParcelaRepository.updateParcelas(parcelasWear)
                Log.d("BitacoraViewModel", "Mensaje procesado instantáneamente")
            } catch (e: Exception) { Log.e("BitacoraViewModel", "Error JSON", e) }
        }
    }

    fun seleccionarParcela(idParcela: String) {
        _selectedParcelId.value = idParcela
        cargarBitacoras(idParcela)
    }

    fun activarRiegoSimulado(idParcela: String) {
        viewModelScope.launch {
            val currentList = _uiState.value.parcelas.toMutableList()
            val index = currentList.indexOfFirst { it.id == idParcela }
            if (index != -1) {
                val p = currentList[index]
                currentList[index] = p.copy(humedad = 45f)
                ParcelaRepository.updateParcelas(currentList.toList())
            }
        }
    }

    fun cargarBitacoras(idParcela: String) {
        viewModelScope.launch {
            val bitacoras = obtenerBitacorasUseCase(idParcela)
            _uiState.value = _uiState.value.copy(bitacoras = bitacoras)
        }
    }

    fun startRecording(outputDir: File) {
        if (_uiState.value.isRecording) return
        try {
            val fileName = "parcela_${_selectedParcelId.value}_${System.currentTimeMillis()}.mp3"
            audioFile = File(outputDir, fileName)
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare(); start()
            }
            _uiState.value = _uiState.value.copy(isRecording = true, recordedTime = "00:00")
            startTimer()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            timerJob?.cancel()
            val finalTime = _uiState.value.recordedTime
            val filePath = audioFile?.absolutePath
            _uiState.value = _uiState.value.copy(isRecording = false, lastAudioPath = filePath)
            guardarBitacora(_selectedParcelId.value, "Nota Parcela ${_selectedParcelId.value}", "Audio grabado (${_uiState.value.recordedTime})", filePath)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun playAudio(path: String?) {
        val targetPath = path ?: return
        if (_uiState.value.isPlaying) stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(targetPath); prepare(); start()
                setOnCompletionListener { _uiState.value = _uiState.value.copy(isPlaying = false); stopPlayback() }
            }
            _uiState.value = _uiState.value.copy(isPlaying = true)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopPlayback() {
        mediaPlayer?.apply { if (isPlaying) stop(); release() }
        mediaPlayer = null
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000); seconds++
                _uiState.value = _uiState.value.copy(recordedTime = String.format("%02d:%02d", seconds / 60, seconds % 60))
            }
        }
    }

    fun guardarBitacora(idParcela: String, titulo: String, descripcion: String, audio: String?) {
        viewModelScope.launch {
            val bitacora = Bitacora(id = (_uiState.value.bitacoras.size + 1), idParcela = idParcela, fecha = Date(), titulo = titulo, descripcion = descripcion, audio = audio, transcripcion = null, sincronizada = false)
            guardarBitacoraUseCase(bitacora)
            cargarBitacoras(idParcela)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Wearable.getMessageClient(getApplication<Application>()).removeListener(this)
        mediaRecorder?.release(); mediaPlayer?.release()
    }
}
