# Módulo :wear

## Descripción
El módulo `:wear` es la extensión del ecosistema EcoViñedos para dispositivos Wear OS. Su función es proporcionar al trabajador de campo alertas críticas de humedad en tiempo real y control de riego de emergencia directamente en la muñeca, permitiendo una reacción inmediata ante condiciones climáticas o de suelo adversas sin necesidad de usar el smartphone.

Funcionalidades:
*   **Monitoreo persistente:** Foreground Service para escucha continua de MQTT.
*   **Alertas Críticas:** Pantalla de emergencia (`AlertaActivity`) que despierta el reloj.
*   **Notas de voz:** Grabación rápida de eventos para la bitácora de campo.
*   **Sincronización:** Recepción de configuración masiva desde el móvil.

## Tecnologías y Dependencias
*   **Wear Compose:** UI optimizada para pantallas circulares y pequeñas.
*   **Foreground Services:** Conexión MQTT persistente en segundo plano.
*   **Google Play Services Wearable:** Capa de datos para sincronización.
*   **Paho MQTT:** Protocolo de mensajería liviano.
*   **Splash Screen API:** Transición suave al iniciar.

## Estructura del Módulo (Todos los archivos)
```text
wear/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/mx/utng/ecoviedos/
│   │   ├── data/
│   │   │   ├── ParcelaRepository.kt
│   │   │   ├── WearableDataService.kt
│   │   │   ├── mqtt/
│   │   │   │   ├── MqttConfig.kt
│   │   │   │   ├── MqttManager.kt
│   │   │   │   └── MqttWearService.kt
│   │   │   └── repository/
│   │   │       └── BitacoraRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Bitacora.kt
│   │   │   │   └── Parcela.kt
│   │   │   └── repository/
│   │   │       └── BitacoraRepository.kt
│   │   └── presentation/
│   │       ├── AlertaActivity.kt
│   │       ├── MainActivity.kt
│   │       └── screens/
│   │           ├── AlertScreen.kt
│   │           ├── BitacoraScreen.kt
│   │           ├── BitacoraViewModel.kt
│   │           ├── IrrigationSuccessScreen.kt
│   │           ├── MyParcelsScreen.kt
│   │           └── ParcelDetailScreen.kt
```

## Arquitectura
Implementa un patrón de **Servicio de Fondo + MVVM**:
*   **Core:** `MqttWearService` centraliza la recepción de datos y detección de umbrales.
*   **View:** Pantallas circulares Compose que observan la caché local.
*   **Data:** `ParcelaRepository` (Caché local) sincronizado por red y Bluetooth.

---

## Código Fuente Completo

### `wear/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

    <uses-feature android:name="android.hardware.type.watch" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault"
        android:usesCleartextTraffic="true">
        <service android:name=".data.WearableDataService" android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
                <data android:scheme="wear" android:host="*" android:pathPrefix="/parcelas_message" />
            </intent-filter>
        </service>
        <service android:name=".data.mqtt.MqttWearService" android:foregroundServiceType="dataSync" android:exported="false" />
        <activity android:name=".presentation.MainActivity" android:exported="true" android:launchMode="singleTop" android:theme="@style/MainActivityTheme.Starting">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".presentation.AlertaActivity" android:exported="false" android:showWhenLocked="true" android:turnScreenOn="true" android:theme="@android:style/Theme.DeviceDefault" />
    </application>
</manifest>
```

### `MqttWearService.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/mqtt/MqttWearService.kt`
```kotlin
package mx.utng.ecoviedos.data.mqtt

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.AlertaActivity
import mx.utng.ecoviedos.presentation.MainActivity

/** Servicio persistente que escucha MQTT y lanza alertas críticas. */
class MqttWearService : Service() {
    private var mqttManager: MqttManager? = null
    private val CHANNEL_ID = "mqtt_service_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, createNotification())
        initializeMqtt()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Servicio Monitoreo", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EcoViñedos")
            .setContentText("Monitoreando parcelas en tiempo real")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent).build()
    }

    private fun initializeMqtt() {
        mqttManager = MqttManager(context = this,
            onSensorsUpdated = { id, hum, temp, humsuel, riego, tiempo -> updateParcelaLocalmente(id, hum, temp, humsuel, riego, tiempo) },
            onRiegoStatusReceived = { id, activo, tiempo -> updateRiegoLocalmente(id, activo, tiempo) },
            onStatusChanged = { Log.d("MqttWearService", "MQTT: $it") }
        )
        mqttManager?.connect()
    }

    private fun updateParcelaLocalmente(id: String, hum: Float, temp: Float, humsuel: Float, riego: Boolean, tiempo: Int) {
        val current = ParcelaRepository.parcelas.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = current[index].copy(humedad = hum, temperatura = temp, humedadSuelo = humsuel, riegoActivo = riego, tiempoRestanteRiego = tiempo)
            current[index] = updated
            ParcelaRepository.updateParcelas(current.toList(), this)
            if (updated.esHumedadCritica()) showUrgentAlert(updated)
        }
    }

    private fun updateRiegoLocalmente(id: String, activo: Boolean, tiempo: Int) {
        val current = ParcelaRepository.parcelas.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(riegoActivo = activo, tiempoRestanteRiego = tiempo)
            ParcelaRepository.updateParcelas(current.toList(), this)
        }
    }

    private fun showUrgentAlert(parcela: Parcela) {
        val fullScreenIntent = Intent(this, AlertaActivity::class.java).apply {
            putExtra("parcela_id", parcela.id); putExtra("parcela", parcela.nombreParcela)
            putExtra("variedad", parcela.variedad); putExtra("humedad", "${parcela.humedadSuelo.toInt()}%")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, parcela.id.hashCode(), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, "critical_alerts")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("¡Humedad Crítica!")
            .setContentText("Parcela ${parcela.nombreParcela} requiere riego.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true).build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(parcela.id.hashCode(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { mqttManager?.disconnect(); super.onDestroy() }
}
```

### `BitacoraViewModel.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/BitacoraViewModel.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.data.mqtt.MqttManager

/** ViewModel para Wear OS que gestiona el estado de las parcelas. */
class BitacoraViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BitacoraUiState())
    val uiState = _uiState.asStateFlow()
    private val _selectedParcelId = MutableStateFlow("")
    val selectedParcelId = _selectedParcelId.asStateFlow()
    private val prefs = application.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
    private val mqtt = MqttManager(application, { _,_,_,_,_,_ -> }, { _,_,_ -> }, { }, { }, { _,_ -> })

    init {
        viewModelScope.launch {
            _selectedParcelId.value = prefs.getString("last_parcel_id", "") ?: ""
            ParcelaRepository.parcelas.collect { _uiState.value = _uiState.value.copy(parcelas = it) }
        }
    }

    /** Selecciona parcela activa. */
    fun seleccionarParcela(id: String) {
        _selectedParcelId.value = id
        prefs.edit().putString("last_parcel_id", id).apply()
    }

    /** Envía comando MQTT de inicio de riego. */
    fun activarRiego(id: String) { mqtt.activarRiego(id, "ON", 10) }
    /** Envía comando MQTT de apagado de riego. */
    fun detenerRiego(id: String) { mqtt.activarRiego(id, "OFF", 0) }
}
```

### `AlertaActivity.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/AlertaActivity.kt`
```kotlin
package mx.utng.ecoviedos.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.*
import mx.utng.ecoviedos.presentation.theme.AppTheme

/** Actividad que despierta el reloj ante alertas críticas. */
class AlertaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        val pId = intent.getStringExtra("parcela_id") ?: ""
        val nombre = intent.getStringExtra("parcela") ?: "P"
        val hum = intent.getStringExtra("humedad") ?: "0%"

        setContent {
            AppTheme {
                // UI UI implementada en AlertUI (Omitida por brevedad en este ejemplo)
            }
        }
    }
}
```

---

## Flujo de Funcionamiento
1.  **Polling MQTT:** El `MqttWearService` recibe telemetría de sensores cada 10-15s directamente de HiveMQ.
2.  **Detección de Umbral:** Si la humedad del suelo cae bajo el umbral configurado, el servicio dispara una notificación de alta prioridad.
3.  **Respuesta Directa:** El trabajador puede detener o iniciar riego manual directamente desde la pantalla circular del reloj.

## Ejecución
1.  Utilizar emulador de **Wear OS 4 (Round)**.
2.  Emparejar con el smartphone administrador.
3.  Permitir notificaciones y uso de sensores.
