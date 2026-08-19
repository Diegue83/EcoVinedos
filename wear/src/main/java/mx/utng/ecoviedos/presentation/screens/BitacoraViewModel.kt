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

/**
 * ViewModel principal para el módulo Wear OS.
 *
 * Gestiona el estado de la UI, la grabación de notas de voz para la bitácora,
 * el control de riego vía MQTT y el temporizador local de riego.
 *
 * @param application Instancia de la aplicación.
 * @param guardarBitacoraUseCase Caso de uso para persistir notas.
 * @param obtenerBitacorasUseCase Caso de uso para recuperar notas grabadas.
 */
class BitacoraViewModel(
    application: Application,
    private val guardarBitacoraUseCase: GuardarBitacoraUseCase,
    private val obtenerBitacorasUseCase: ObtenerBitacorasUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BitacoraUiState())
    /** Estado de la interfaz de usuario. */
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    private val _selectedParcelId = MutableStateFlow("")
    /** ID de la parcela actualmente visualizada. */
    val selectedParcelId: StateFlow<String> = _selectedParcelId.asStateFlow()

    private val _showAllParcels = MutableStateFlow(false)
    /** Flag para mostrar u ocultar parcelas sin sensores vinculados. */
    val showAllParcels: StateFlow<Boolean> = _showAllParcels.asStateFlow()

    private val prefs = application.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private var audioFile: File? = null
    private var irrigationTimerJob: Job? = null
    
    private val mqttManager = MqttManager(
        context = application,
        onSensorsUpdated = { _, _, _, _, _, _ -> },
        onRiegoStatusReceived = { _, _, _ -> },
        onStatusChanged = { }
    )

    init {
        viewModelScope.launch {
            _selectedParcelId.value = prefs.getString("last_parcel_id", "") ?: ""
            
            ParcelaRepository.parcelas.collect { parcelas ->
                if (parcelas.isNotEmpty() && _selectedParcelId.value.isBlank()) {
                    _selectedParcelId.value = parcelas.first().id
                }
                _uiState.value = _uiState.value.copy(parcelas = parcelas)
                startIrrigationTimer()
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager.connect()
        }
    }

    /**
     * Inicia el temporizador local que decrementa el tiempo restante de riego cada segundo.
     */
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

    /**
     * Cambia la parcela activa y guarda la preferencia.
     *
     * @param idParcela Identificador de la parcela seleccionada.
     */
    fun seleccionarParcela( idParcela: String) {
        _selectedParcelId.value = idParcela
        prefs.edit().putString("last_parcel_id", idParcela).apply()
        cargarBitacoras(idParcela)
    }

    /** Alterna el filtrado de parcelas con/sin sensores. */
    fun toggleShowAllParcels() {
        _showAllParcels.value = !_showAllParcels.value
    }

    /** Envía comando MQTT para iniciar riego. */
    fun activarRiego(idParcela: String) {
        mqttManager.activarRiego(idParcela, "ON", 10)
    }

    /** Envía comando MQTT para detener riego. */
    fun detenerRiego(idParcela: String) {
        mqttManager.activarRiego(idParcela, "OFF", 0)
    }

    /** Carga las notas de voz locales de una parcela. */
    fun cargarBitacoras(idParcela: String) {
        viewModelScope.launch {
            val filesDir = getApplication<Application>().filesDir
            val bitacoras = filesDir.listFiles()?.filter {
                it.isFile && it.name.startsWith("parcela_${idParcela}_")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
            _uiState.value = _uiState.value.copy(bitacoras = bitacoras)
        }
    }

    /** Inicia la grabación de audio desde el micrófono. */
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
            }
            _uiState.value = _uiState.value.copy(isRecording = true, recordedTime = "00:00")
            startTimer()
        } catch (e: Exception) { e.printStackTrace() }
    }

    /** Detiene la grabación y guarda el registro en la base de datos local. */
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

    /** Reproduce una nota de voz guardada. */
    fun playAudio(path: String?) {
        val targetPath = path ?: return
        val file = File(targetPath)
        if (!file.exists()) return

        if (_uiState.value.isPlaying) {
            mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare(); start()
                setOnCompletionListener {
                    release(); mediaPlayer = null
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                }
            }
            _uiState.value = _uiState.value.copy(isPlaying = true)
        } catch (e: Exception) { e.printStackTrace() }
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

    /** Guarda la entidad de bitácora. */
    fun guardarBitacora(context: Context,idParcela: String, titulo: String, descripcion: String, audio: String?) {
        viewModelScope.launch {
            val bitacora = Bitacora(id = (_uiState.value.bitacoras.size + 1), idParcela = idParcela, fecha = Date(), titulo = titulo, descripcion = descripcion, audio = audio, transcripcion = null, sincronizada = false)
            guardarBitacoraUseCase(bitacora)
            cargarBitacoras(idParcela)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager.disconnect()
        mediaRecorder?.release(); mediaPlayer?.release()
    }
}
