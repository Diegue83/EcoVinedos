package mx.utng.ecoviedos.presentation.screens

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.data.mqtt.MqttManager
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
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BitacoraUiState())
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    private val _selectedParcelId = MutableStateFlow("4")
    val selectedParcelId: StateFlow<String> = _selectedParcelId.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private var audioFile: File? = null
    private var mqttManager: MqttManager? = null
    private var irrigationTimerJob: Job? = null

    init {
        // Inicializar MQTT para recibir datos directamente del broker
        mqttManager = MqttManager(
            onSensorsUpdated = { id, hum, temp, humsuel, riego, tiempo ->
                updateParcelaLocalmente(id, hum, temp, humsuel, riego, tiempo)
            },
            onRiegoStatusReceived = { id, activo, tiempo ->
                updateRiegoLocalmente(id, activo, tiempo)
            }
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
        }

        viewModelScope.launch {
            ParcelaRepository.parcelas.collect { parcelas ->
                if (parcelas.isNotEmpty() && _selectedParcelId.value.isBlank()) {
                    _selectedParcelId.value = parcelas.first().id
                }
                _uiState.value = _uiState.value.copy(parcelas = parcelas)
                startIrrigationTimer()
            }
        }
    }

    private fun startIrrigationTimer() {
        irrigationTimerJob?.cancel()
        irrigationTimerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                val currentParcelas = _uiState.value.parcelas
                if (currentParcelas.any { it.riegoActivo }) {
                    val updatedList = currentParcelas.map { parcela ->
                        if (parcela.riegoActivo) {
                            val nextTime = parcela.tiempoRestanteRiego - 1
                            // En el reloj solo mostramos, no disparamos notificaciones push pesadas (el móvil lo hace)
                            if (nextTime <= 0 && parcela.tipoRiego == "AUTO") {
                                parcela.copy(tiempoRestanteRiego = 0, riegoActivo = false)
                            } else {
                                parcela.copy(tiempoRestanteRiego = nextTime)
                            }
                        } else {
                            parcela
                        }
                    }
                    _uiState.value = _uiState.value.copy(parcelas = updatedList)
                }
            }
        }
    }

    private fun updateParcelaLocalmente(id: String, hum: Float, temp: Float, humsuel: Float, riego: Boolean, tiempo: Int) {
        val currentList = _uiState.value.parcelas.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedParcela = currentList[index].copy(
                humedad = hum, 
                temperatura = temp,
                humedadSuelo = humsuel,
                riegoActivo = riego,
                tiempoRestanteRiego = tiempo
            )
            currentList[index] = updatedParcela
            viewModelScope.launch(Dispatchers.Main) {
                ParcelaRepository.updateParcelas(currentList.toList())
            }
        }
    }

    private fun updateRiegoLocalmente(id: String, activo: Boolean, tiempo: Int) {
        val currentList = _uiState.value.parcelas.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedParcela = currentList[index].copy(
                riegoActivo = activo,
                tiempoRestanteRiego = tiempo
            )
            currentList[index] = updatedParcela
            viewModelScope.launch(Dispatchers.Main) {
                ParcelaRepository.updateParcelas(currentList.toList())
            }
        }
    }

    fun seleccionarParcela( idParcela: String) {
        _selectedParcelId.value = idParcela
        cargarBitacoras(idParcela)
    }

    fun activarRiego(idParcela: String) {
        mqttManager?.activarRiego(idParcela, "ON", 10)
    }

    fun detenerRiego(idParcela: String) {
        mqttManager?.activarRiego(idParcela, "OFF", 0)
    }



    fun cargarBitacoras(idParcela: String) {
        viewModelScope.launch {
            val filesDir = getApplication<Application>().filesDir

            val bitacoras = filesDir
                .listFiles()
                ?.filter {
                    it.isFile && it.name.startsWith("parcela_${idParcela}_")
                }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()

            _uiState.value = _uiState.value.copy(bitacoras = bitacoras)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(context: Context, outputDir: File) {
        if (_uiState.value.isRecording) return
        try {
            val fileName = "parcela_${_selectedParcelId.value}_${System.currentTimeMillis()}.mp3"
            audioFile = File(outputDir, fileName)
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare(); start()
                Log.d("Audio", "Recorder iniciado")
            }
            _uiState.value = _uiState.value.copy(isRecording = true, recordedTime = "00:00")
            startTimer()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopRecording(context: Context) {
        if (!_uiState.value.isRecording) return
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            timerJob?.cancel()
            val finalTime = _uiState.value.recordedTime
            val filePath = audioFile?.absolutePath
            _uiState.value = _uiState.value.copy(isRecording = false, lastAudioPath = filePath)
            guardarBitacora(context,_selectedParcelId.value, "Nota Parcela ${_selectedParcelId.value}", "Audio grabado ($finalTime)", filePath)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun playAudio(path: String?) {
        val targetPath = path ?: return

        val file = File(targetPath)

        Log.d("Audio", "Ruta: ${file.absolutePath}")
        Log.d("Audio", "Existe: ${file.exists()}")
        Log.d("Audio", "Tamaño: ${file.length()}")

        if (!file.exists()) {
            Log.e("Audio", "El archivo no existe")
            return
        }

        if (_uiState.value.isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()

                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                }
            }

            _uiState.value = _uiState.value.copy(isPlaying = true)
            Log.d("Audio", "Reproduciendo: $targetPath")

        } catch (e: Exception) {
            Log.e("Audio", "Error reproduciendo", e)
        }
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

    fun guardarBitacora(context: Context,idParcela: String, titulo: String, descripcion: String, audio: String?) {
        viewModelScope.launch {
            val bitacora = Bitacora(id = (_uiState.value.bitacoras.size + 1), idParcela = idParcela, fecha = Date(), titulo = titulo, descripcion = descripcion, audio = audio, transcripcion = null, sincronizada = false)
            guardarBitacoraUseCase(bitacora)
            cargarBitacoras(idParcela)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
        mediaRecorder?.release(); mediaPlayer?.release()
    }
}
