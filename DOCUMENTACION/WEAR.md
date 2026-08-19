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
│   │           ├── BitacoraUiState.kt
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

### `ParcelaRepository.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/ParcelaRepository.kt`
```kotlin
package mx.utng.ecoviedos.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

/**
 * Gestor de persistencia local y caché para el módulo Wear OS.
 */
object ParcelaRepository {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas

    /** Inicializa la caché desde disco. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
        val json = prefs.getString("parcelas_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Parcela>>() {}.type
                _parcelas.value = Gson().fromJson(json, type)
            } catch (e: Exception) {}
        }
    }

    /** Actualiza parcelas y las guarda en SharedPreferences. */
    fun updateParcelas(newList: List<Parcela>, context: Context? = null) {
        _parcelas.value = newList
        context?.let {
            val prefs = it.getSharedPreferences("parcela_cache", Context.MODE_PRIVATE)
            prefs.edit().putString("parcelas_list", Gson().toJson(newList)).apply()
        }
    }
}
```

### `WearableDataService.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/WearableDataService.kt`
```kotlin
package mx.utng.ecoviedos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.AlertaActivity
import mx.utng.ecoviedos.presentation.MainActivity
import java.util.Date

/** Escucha mensajes del smartphone para sincronización masiva. */
class WearableDataService : WearableListenerService() {
    private val gson = Gson()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/parcelas_message") {
            val json = String(messageEvent.data, Charsets.UTF_8)
            try {
                val itemType = object : TypeToken<List<ParcelaMap>>() {}.type
                val parcelasMobile: List<ParcelaMap> = gson.fromJson(json, itemType)
                val parcelasWear = parcelasMobile.map { m ->
                    Parcela(
                        id = m._id,
                        nombreParcela = m.nombreParcela ?: "Parcela ${m._id}",
                        variedad = m.variedad ?: "",
                        areaM2 = m.areaM2,
                        umbralHumedad = m.umbralHumedad,
                        umbralTemp = m.umbralTemp,
                        umbralHumedadSuelo = m.umbralHumedadSuelo ?: 40f,
                        indiceMaduracion = m.indiceMaduracion,
                        fechaCosecha = m.fechaCosecha ?: Date(),
                        activa = m.activa,
                        humedad = m.humedad,
                        temperatura = m.temperatura,
                        humedadSuelo = m.humedadSuelo ?: 0f,
                        riegoActivo = m.riegoActivo ?: false,
                        tiempoRestanteRiego = (m.tiempoRestanteRiego ?: 0) * 60,
                        tipoRiego = m.tipoRiego ?: "MANUAL",
                        nodoVinculado = m.nodoVinculado
                    )
                }
                Handler(Looper.getMainLooper()).post {
                    ParcelaRepository.updateParcelas(parcelasWear, this@WearableDataService)
                }
            } catch (e: Exception) { }
        }
    }
}
```

### `MqttManager.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/mqtt/MqttManager.kt`
```kotlin
package mx.utng.ecoviedos.data.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

/** Gestor MQTT especializado para Wear OS. */
class MqttManager(
    private val context: Context,
    private val onSensorsUpdated: (String, Float, Float, Float, Boolean, Int) -> Unit,
    private val onRiegoStatusReceived: (String, Boolean, Int) -> Unit,
    private val onStatusChanged: (String) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val clientId = "WearClient_${System.currentTimeMillis()}"

    /** Inicia conexión persistente. */
    fun connect() {
        try {
            mqttClient = MqttClient(MqttConfig.BROKER_URL, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isAutomaticReconnect = true
            }
            mqttClient?.connect(options)
        } catch (e: Exception) { }
    }

    /** Envía comando de riego. */
    fun activarRiego(idParcela: String, comando: String = "ON", duracion: Int = 1) {
        val payload = JSONObject().apply { put("comando", comando); put("duracion", duracion) }.toString()
        mqttClient?.publish("vinedo/parcela/$idParcela/control", MqttMessage(payload.toByteArray()))
    }

    fun disconnect() { mqttClient?.disconnect() }
}
```

### `MqttWearService.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/mqtt/MqttWearService.kt`
```kotlin
package mx.utng.ecoviedos.data.mqtt

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.AlertaActivity
import mx.utng.ecoviedos.presentation.MainActivity

/** Servicio en primer plano para monitoreo MQTT y alertas. */
class MqttWearService : Service() {
    private var mqttManager: MqttManager? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1001, createNotification())
        initializeMqtt()
    }

    private fun initializeMqtt() {
        mqttManager = MqttManager(this, 
            { id, h, t, hs, r, time -> updateParcelaLocalmente(id, h, t, hs, r, time) },
            { id, a, time -> updateRiegoLocalmente(id, a, time) },
            { }
        )
        mqttManager?.connect()
    }

    private fun updateParcelaLocalmente(id: String, h: Float, t: Float, hs: Float, r: Boolean, time: Int) {
        val current = ParcelaRepository.parcelas.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = current[index].copy(humedad = h, temperatura = t, humedadSuelo = hs, riegoActivo = r, tiempoRestanteRiego = time)
            current[index] = updated
            ParcelaRepository.updateParcelas(current, this)
            if (updated.esHumedadCritica()) showUrgentAlert(updated)
        }
    }

    private fun showUrgentAlert(parcela: Parcela) {
        val intent = Intent(this, AlertaActivity::class.java).apply {
            putExtra("parcela_id", parcela.id); putExtra("parcela", parcela.nombreParcela)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, "critical").setContentTitle("Humedad Crítica").setFullScreenIntent(pending, true).build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification)
    }

    private fun createNotification(): Notification = NotificationCompat.Builder(this, "mqtt").setContentTitle("Monitoreo Activo").build()
    override fun onBind(intent: Intent?): IBinder? = null
}
```

### `BitacoraRepositoryImpl.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/data/repository/BitacoraRepositoryImpl.kt`
```kotlin
package mx.utng.ecoviedos.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

/** Implementación en memoria del repositorio de bitácoras. */
class BitacoraRepositoryImpl : BitacoraRepository {
    private val bitacoras = mutableListOf<Bitacora>()
    private val mutex = Mutex()

    override suspend fun guardarBitacora(bitacora: Bitacora) { mutex.withLock { bitacoras.add(bitacora) } }
    override suspend fun obtenerTodasLasBitacoras(): List<Bitacora> = mutex.withLock { bitacoras.toList() }
    override suspend fun eliminarBitacora(id: Int) { mutex.withLock { bitacoras.removeAll { it.id == id } } }
    override fun obtenerAudiosPorParcela(idParcela: String): List<java.io.File> = emptyList()
}
```

### `Bitacora.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/domain/model/Bitacora.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Modelo de bitácora para el reloj. */
data class Bitacora(
    val id: Int, val idParcela: String, val fecha: Date,
    var titulo: String, var descripcion: String, var audio: String?,
    var transcripcion: String?, var sincronizada: Boolean
)
```

### `Parcela.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/domain/model/Parcela.kt`
```kotlin
package mx.utng.ecoviedos.domain.model

import java.util.Date

/** Modelo de parcela optimizado para Wear OS. */
data class Parcela(
    val id: String, val nombreParcela: String, val variedad: String, val areaM2: Int,
    val umbralHumedad: Float, val umbralTemp: Float, val umbralHumedadSuelo: Float,
    val indiceMaduracion: Float, val fechaCosecha: Date, val activa: Boolean,
    val humedad: Float, val temperatura: Float, val humedadSuelo: Float,
    val riegoActivo: Boolean = false, val tiempoRestanteRiego: Int = 0,
    val tipoRiego: String = "MANUAL", val nodoVinculado: String? = null
) {
    fun esHumedadCritica(): Boolean = humedadSuelo < umbralHumedadSuelo && !riegoActivo
}
```

### `AlertaActivity.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/AlertaActivity.kt`
```kotlin
package mx.utng.ecoviedos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.*
import mx.utng.ecoviedos.presentation.theme.AppTheme

/** Actividad que despierta el reloj ante emergencias. */
class AlertaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                       android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        setContent { AppTheme { /* UI Alerta */ } }
    }
}
```

### `MainActivity.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/MainActivity.kt`
```kotlin
package mx.utng.ecoviedos.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import mx.utng.ecoviedos.presentation.theme.AppTheme

/** Actividad principal con navegación circular. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startService(Intent(this, mx.utng.ecoviedos.data.mqtt.MqttWearService::class.java))
        setContent { AppTheme { /* Navigation Pager */ } }
    }
}
```

### `AlertScreen.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/AlertScreen.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.*

/** Pantalla de UI para alertas críticas. */
@Composable
fun AlertScreen(id: String, name: String, onActivate: () -> Unit) {
    Column {
        Text("¡ALERTA HUMEDAD!")
        Text(name)
        Button(onClick = onActivate) { Text("ACTIVAR RIEGO") }
    }
}
```

### `BitacoraScreen.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/BitacoraScreen.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Text

/** Pantalla de registro de notas de voz. */
@Composable
fun BitacoraScreen(viewModel: BitacoraViewModel, idParcela: String) {
    ScalingLazyColumn {
        item { Text("NOTAS DE VOZ") }
        /* Listado de grabaciones */
    }
}
```

### `BitacoraViewModel.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/BitacoraViewModel.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import mx.utng.ecoviedos.data.mqtt.MqttManager

/** ViewModel para control de UI y MQTT. */
class BitacoraViewModel(application: Application, val guardarUC: Any, val obtenerUC: Any) : AndroidViewModel(application) {
    private val mqtt = MqttManager(application, {_,_,_,_,_,_ ->}, {_,_,_ ->}, {})
    fun activarRiego(id: String) { mqtt.activarRiego(id) }
}
```

### `MyParcelsScreen.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/MyParcelsScreen.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Text

/** Listado de parcelas del trabajador. */
@Composable
fun MyParcelsScreen(viewModel: BitacoraViewModel, onParcelClick: (String) -> Unit) {
    ScalingLazyColumn {
        item { Text("MIS PARCELAS") }
        /* items(sortedParcelas) */
    }
}
```

### `ParcelDetailScreen.kt`
Ubicación: `wear/src/main/java/mx/utng/ecoviedos/presentation/screens/ParcelDetailScreen.kt`
```kotlin
package mx.utng.ecoviedos.presentation.screens

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.Text

/** Detalle de telemetría en pantalla circular. */
@Composable
fun ParcelDetailScreen(viewModel: BitacoraViewModel, idParcela: String) {
    Text("DETALLE PARCELA")
    /* Medidor de humedad central */
}
```
