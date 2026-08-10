package mx.utng.ecoviedos.presentation.admin

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.ble.BleManager
import org.json.JSONObject

/**
 * Estados del proceso de configuración de hardware vía BLE.
 */
sealed class BleUiState {
    data object Idle : BleUiState()
    data object Scanning : BleUiState()
    data object Connecting : BleUiState()
    data object Connected : BleUiState()
    data object Sending : BleUiState()
    data class VerifyingWiFi(val message: String) : BleUiState()
    data object Success : BleUiState()
    data class Error(val mensaje: String) : BleUiState()
}

/**
 * ViewModel que gestiona la vinculación de nodos IoT mediante Bluetooth Low Energy.
 */
class DeviceConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)
    
    private val _uiState = MutableStateFlow<BleUiState>(BleUiState.Idle)
    val uiState: StateFlow<BleUiState> = _uiState.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var selectedDevice: BluetoothDevice? = null

    /**
     * Receptor de eventos del sistema para detectar cambios en el estado del adaptador Bluetooth.
     */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                checkBluetoothStatus()
            }
        }
    }

    init {
        checkBluetoothStatus()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        application.registerReceiver(bluetoothReceiver, filter)
    }

    /**
     * Sincroniza el estado local de activación del Bluetooth con el adaptador del sistema.
     */
    fun checkBluetoothStatus() {
        _isBluetoothEnabled.value = bleManager.isBluetoothEnabled()
    }

    /**
     * Inicia el proceso de escaneo de dispositivos BLE cercanos.
     */
    fun startScanning() {
        checkBluetoothStatus()
        if (!_isBluetoothEnabled.value) {
            _uiState.value = BleUiState.Error("El Bluetooth está desactivado. Por favor, actívalo para continuar.")
            return
        }
        _discoveredDevices.value = emptyList()
        _uiState.value = BleUiState.Scanning
        bleManager.startScan { device ->
            viewModelScope.launch {
                val currentList = _discoveredDevices.value.toMutableList()
                if (currentList.none { it.address == device.address }) {
                    currentList.add(device)
                    _discoveredDevices.value = currentList
                }
            }
        }
    }

    /**
     * Detiene el escaneo de dispositivos BLE.
     */
    fun stopScanning() {
        bleManager.stopScan()
    }

    /**
     * Establece una conexión GATT con el dispositivo seleccionado e inicia el monitoreo de notificaciones.
     * 
     * @param device Dispositivo Bluetooth a conectar.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        selectedDevice = device
        stopScanning()
        _uiState.value = BleUiState.Connecting
        
        bleManager.connect(device.address) { state ->
            viewModelScope.launch {
                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _uiState.value = BleUiState.Connected
                        // Al conectar, habilitamos las notificaciones de estado
                        bleManager.enableStatusNotifications { jsonResponse ->
                            handleFeedback(jsonResponse)
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        if (_uiState.value !is BleUiState.Success) {
                            _uiState.value = BleUiState.Error("Dispositivo desconectado")
                        }
                    }
                }
            }
        }
    }

    /**
     * Procesa la respuesta JSON enviada por el ESP32 tras el intento de configuración.
     * 
     * @param json Cadena de texto recibida por BLE.
     */
    private fun handleFeedback(json: String) {
        viewModelScope.launch {
            try {
                val cleanJson = json.trim().substringAfter("{").substringBeforeLast("}")
                val finalJson = "{$cleanJson}"
                
                val obj = JSONObject(finalJson)
                val status = obj.optString("status", "").lowercase()
                val message = obj.optString("message", "Procesando...")

                when (status) {
                    "ok" -> _uiState.value = BleUiState.Success
                    "error" -> _uiState.value = BleUiState.Error(message)
                    else -> _uiState.value = BleUiState.VerifyingWiFi(message)
                }
            } catch (e: Exception) {
                val lower = json.lowercase()
                when {
                    lower.contains("\"status\":\"ok\"") || lower.contains("conectado") -> _uiState.value = BleUiState.Success
                    lower.contains("\"status\":\"error\"") || lower.contains("error") -> _uiState.value = BleUiState.Error(json)
                    else -> _uiState.value = BleUiState.VerifyingWiFi(json)
                }
            }
        }
    }

    /**
     * Envía las credenciales WiFi y el ID de la parcela al nodo mediante una característica BLE.
     * 
     * @param ssid Nombre de la red WiFi.
     * @param pass Contraseña de la red WiFi.
     * @param parcelaId Identificador de la parcela a vincular.
     * @param nombreParcela Nombre descriptivo de la parcela.
     */
    fun sendConfig(ssid: String, pass: String, parcelaId: String, nombreParcela: String) {
        _uiState.value = BleUiState.Sending
        
        val json = JSONObject().apply {
            put("ssid", ssid)
            put("password", pass)
            put("station_id", parcelaId)
        }.toString()

        bleManager.sendConfig(json) { success ->
            if (!success) {
                _uiState.value = BleUiState.Error("Error al enviar configuración vía BLE")
            }
        }
    }

    /**
     * Limpia el estado de error y permite reintentar el proceso.
     */
    fun clearError() {
        _uiState.value = BleUiState.Connected
    }

    /**
     * Reinicia el estado del ViewModel y desconecta cualquier dispositivo activo.
     */
    fun resetState() {
        stopScanning()
        _uiState.value = BleUiState.Idle
        _discoveredDevices.value = emptyList()
        bleManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
        bleManager.disconnect()
        getApplication<Application>().unregisterReceiver(bluetoothReceiver)
    }
}
