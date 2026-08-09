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

sealed class BleUiState {
    data object Idle : BleUiState()
    data object Scanning : BleUiState()
    data object Connecting : BleUiState()
    data object Connected : BleUiState()
    data object Sending : BleUiState()
    data class VerifyingWiFi(val message: String) : BleUiState()
    data object Success : BleUiState()
    data class Error(val message: String) : BleUiState()
}

class DeviceConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)
    
    private val _uiState = MutableStateFlow<BleUiState>(BleUiState.Idle)
    val uiState: StateFlow<BleUiState> = _uiState.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var selectedDevice: BluetoothDevice? = null

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

    fun checkBluetoothStatus() {
        _isBluetoothEnabled.value = bleManager.isBluetoothEnabled()
    }

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

    fun stopScanning() {
        bleManager.stopScan()
    }

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

    private fun handleFeedback(json: String) {
        viewModelScope.launch {
            try {
                val obj = JSONObject(json)
                val status = obj.optString("status", "")
                val message = obj.optString("message", "Procesando...")

                when (status) {
                    "ok" -> _uiState.value = BleUiState.Success
                    "error" -> _uiState.value = BleUiState.Error(message)
                    else -> _uiState.value = BleUiState.VerifyingWiFi(message)
                }
            } catch (e: Exception) {
                // Si no es JSON, mostrar el texto bruto
                _uiState.value = BleUiState.VerifyingWiFi(json)
            }
        }
    }

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

    fun clearError() {
        _uiState.value = BleUiState.Connected
    }

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
