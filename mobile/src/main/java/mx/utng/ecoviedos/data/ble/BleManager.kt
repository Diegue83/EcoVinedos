package mx.utng.ecoviedos.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

/**
 * Gestor de comunicaciones Bluetooth Low Energy (BLE).
 * 
 * Centraliza la lógica de escaneo, conexión GATT, lectura/escritura de características
 * y recepción de notificaciones desde dispositivos periféricos (ESP32).
 * 
 * @property context Contexto de la aplicación necesario para acceder a los servicios de sistema.
 */
class BleManager(private val context: Context) {

    companion object {
        // UUIDs: Asegúrate de que coincidan con BluetoothConfig.h en tu ESP32
        val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val CONFIG_CHAR_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val STATUS_CHAR_UUID: UUID = UUID.fromString("0b9a3f9e-2a2c-4c9a-9d7a-5a9f0b0e2b0d")
        private const val TAG = "BleManager"
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    /**
     * Verifica si el adaptador Bluetooth está encendido.
     * 
     * @return true si el Bluetooth está disponible y activo.
     */
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    private var bluetoothGatt: BluetoothGatt? = null

    private var onDeviceDiscovered: ((BluetoothDevice) -> Unit)? = null
    private var onConnectionStateChanged: ((Int) -> Unit)? = null
    private var onDataSent: ((Boolean) -> Unit)? = null
    private var onNotificationReceived: ((String) -> Unit)? = null

    /**
     * Callback invocado por el sistema durante el escaneo BLE.
     */
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = result.scanRecord?.deviceName ?: device.name

            if (name != null) {
                Log.d(TAG, "Dispositivo encontrado: $name [${device.address}]")
                onDeviceDiscovered?.invoke(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Error de escaneo: $errorCode")
        }
    }

    /**
     * Callback principal para el manejo de la conexión GATT y descubrimiento de servicios.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                val errorMsg = when(status) {
                    133 -> "Error 133: El sistema BLE está saturado o el dispositivo rechazó la conexión. Intenta reiniciar el Bluetooth del celular."
                    8 -> "Error 8: Timeout de conexión. El dispositivo está fuera de rango o apagado."
                    1 -> "Error 1 (GATT_ERROR): El dispositivo no respondió a la solicitud de conexión."
                    else -> "Error GATT: status=$status"
                }
                Log.e(TAG, errorMsg)
                disconnect()
                onConnectionStateChanged?.invoke(BluetoothProfile.STATE_DISCONNECTED)
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "GATT Conectado. Solicitando MTU mayor...")
                gatt.requestMtu(512) 
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "GATT Desconectado.")
                onConnectionStateChanged?.invoke(newState)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "MTU cambiado a: $mtu, status: $status")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Servicios descubiertos con éxito.")
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    Log.i(TAG, "Servicio EcoViñedos encontrado.")
                    onConnectionStateChanged?.invoke(BluetoothProfile.STATE_CONNECTED)
                } else {
                    Log.e(TAG, "Servicio EcoViñedos NO encontrado en este dispositivo.")
                    disconnect()
                    onConnectionStateChanged?.invoke(BluetoothProfile.STATE_DISCONNECTED)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (characteristic.uuid == CONFIG_CHAR_UUID) {
                Log.d(TAG, "Escritura completada. Status: $status")
                onDataSent?.invoke(status == BluetoothGatt.GATT_SUCCESS)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == STATUS_CHAR_UUID) {
                val data = String(characteristic.value).trim().replace("\u0000", "")
                Log.d(TAG, "Notificación recibida: $data")
                onNotificationReceived?.invoke(data)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            if (characteristic.uuid == STATUS_CHAR_UUID) {
                val data = String(value).trim().replace("\u0000", "")
                Log.d(TAG, "Notificación recibida (v2): $data")
                onNotificationReceived?.invoke(data)
            }
        }
    }

    /**
     * Inicia el escaneo de dispositivos de bajo consumo.
     * 
     * @param onDiscovered Callback invocado cada vez que se detecta un dispositivo válido.
     */
    @SuppressLint("MissingPermission")
    fun startScan(onDiscovered: (BluetoothDevice) -> Unit) {
        onDeviceDiscovered = onDiscovered
        val scanner = adapter.bluetoothLeScanner
        if (scanner != null) {
            Log.d(TAG, "Iniciando escaneo real...")
            scanner.startScan(scanCallback)
        } else {
            Log.e(TAG, "El escáner BLE no está disponible (¿Bluetooth apagado?)")
        }
    }

    /**
     * Detiene el escaneo activo de dispositivos BLE.
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
    }

    /**
     * Inicia una solicitud de conexión GATT con un dispositivo específico.
     * 
     * @param address Dirección MAC del dispositivo.
     * @param onStateChange Callback para notificar cambios en el estado de la conexión.
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String, onStateChange: (Int) -> Unit) {
        onConnectionStateChanged = onStateChange
        disconnect()
        
        val device = adapter.getRemoteDevice(address)
        Log.d(TAG, "Intentando conectar a ${device.address}...")

        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        }, 500)
    }

    /**
     * Envía una cadena JSON al dispositivo mediante la característica de configuración.
     * 
     * @param json Contenido a enviar.
     * @param onResult Callback que indica si la escritura fue exitosa.
     */
    @SuppressLint("MissingPermission")
    fun sendConfig(json: String, onResult: (Boolean) -> Unit) {
        onDataSent = onResult
        val service = bluetoothGatt?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CONFIG_CHAR_UUID)

        if (characteristic != null) {
            Log.d(TAG, "Enviando JSON: $json")
            characteristic.value = json.toByteArray()
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
            if (!success) onResult(false)
        } else {
            Log.e(TAG, "No se encontró la característica de configuración.")
            onResult(false)
        }
    }

    /**
     * Habilita las notificaciones asíncronas para la característica de estado del nodo.
     * 
     * @param onReceived Callback invocado cada vez que llega un nuevo mensaje del nodo.
     */
    @SuppressLint("MissingPermission")
    fun enableStatusNotifications(onReceived: (String) -> Unit) {
        onNotificationReceived = onReceived
        val service = bluetoothGatt?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(STATUS_CHAR_UUID)

        if (characteristic != null) {
            bluetoothGatt?.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
                Log.d(TAG, "Notificaciones habilitadas para estado.")
            }
        }
    }

    /**
     * Cierra la conexión GATT activa y libera los recursos del adaptador.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d(TAG, "Conexión cerrada y recursos liberados.")
    }
}
