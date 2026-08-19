# Módulo :tv

## Descripción
El módulo `:tv` es una aplicación de Android TV diseñada para el monitoreo pasivo y continuo en la bodega. Utiliza una interfaz optimizada para pantallas grandes (Leanback) y permite al personal supervisar el estado de la cava y las parcelas mediante un panel reactivo que consume datos en tiempo real vía MQTT.

Responsabilidades:
*   **Dashboard Central:** Resumen de promedios de temperatura y humedad global.
*   **Monitoreo de Cava:** Detalle por sección de las condiciones de almacenamiento.
*   **Vinculación:** Sistema de emparejamiento con el móvil mediante códigos QR.
*   **Información de Actividades:** Visualización de eventos programados para visitantes.

## Tecnologías y Dependencias
*   **Compose for TV:** Componentes específicos para navegación por DPAD.
*   **Retrofit:** Sincronización del estado de sesión.
*   **MQTT Paho:** Telemetría instantánea.
*   **ZXing:** Generación de códigos QR de vinculación.
*   **Coil:** Carga de posters e imágenes de eventos.

## Estructura del Módulo (Todos los archivos)
```text
tv/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── java/mx/utng/ecoviedos/tv/
        ├── MainActivity.kt
        └── presentation/
            ├── ActivitiesScreen.kt
            ├── CavaDetailScreen.kt
            ├── MainTvScreen.kt
            ├── PairingScreen.kt
            ├── QrGenerator.kt
            ├── TvDashboardScreen.kt
            ├── TvViewModel.kt
            ├── events/
            │   └── EventsScreen.kt
            ├── tourism/
            │   └── TourismScreen.kt
            └── ui/theme/
                ├── Color.kt
                └── Theme.kt
```

## Arquitectura
Implementa **MVVM reactivo**:
1.  **TvViewModel:** Mantiene un polling constante del estado de vinculación y gestiona la conexión MQTT compartida.
2.  **State Management:** Utiliza `TvUiState` (sealed class) para manejar los estados de Carga, No Vinculado, Vinculado y Error.
3.  **UI:** Pantallas modulares que heredan el estado global del ViewModel.

---

## Código Fuente Completo

### `tv/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.ecoviedos.tv"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.utng.ecoviedos.tv"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../ecovinedos-release.jks")
            storePassword = "<CONTRASEÑA>"
            keyAlias = "ecovinedos"
            keyPassword = "<CONTRASEÑA>"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
    implementation("androidx.tv:tv-material:1.0.0-alpha11")
    implementation(libs.zxing.core)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```

### `tv/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.touchscreen" android:required="false" />
    <uses-feature android:name="android.software.leanback" android:required="false" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:banner="@mipmap/ic_launcher"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.EcoViñedos"
        android:usesCleartextTraffic="true">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### `MainActivity.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/MainActivity.kt`
```kotlin
package mx.utng.ecoviedos.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import mx.utng.ecoviedos.tv.ui.theme.EcoViñedosTheme
import mx.utng.ecoviedos.tv.presentation.MainTvScreen

/**
 * Punto de entrada para la aplicación de Android TV.
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoViñedosTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    MainTvScreen()
                }
            }
        }
    }
}
```

### `ActivitiesScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/ActivitiesScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

/**
 * Pantalla que muestra el carrusel horizontal de actividades y eventos.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivitiesScreen(viewModel: TourismViewModel = viewModel()) {
    val activities by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarEventos() }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F100D)).padding(32.dp)) {
        Text("Actividades y experiencias", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFB4F391)) }
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(end = 32.dp)
            ) {
                items(activities.size) { index ->
                    ActivityCard(
                        title = activities[index].titulo,
                        desc = activities[index].descripcion,
                        price = "${activities[index].precio} MXN",
                        tag = if(activities[index].tipo == "TOURISM") "Turismo" else "Evento",
                        imageUrl = activities[index].imagenUrl,
                        bgColor = if(activities[index].tipo == "TOURISM") Color(0xFF2E7D32) else Color(0xFF1565C0),
                        modifier = Modifier.width(300.dp)
                    )
                }
            }
        }
    }
}

/** Tarjeta individual de actividad optimizada para TV. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivityCard(title: String, desc: String, price: String, tag: String, imageUrl: String?, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        onClick = {},
        modifier = modifier.fillMaxHeight(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = bgColor.copy(alpha = 0.2f), focusedContainerColor = bgColor.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) { AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.6f) }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(desc, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 4)
                Spacer(Modifier.weight(1f))
                Text(price, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
```

### `CavaDetailScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/CavaDetailScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import mx.utng.ecoviedos.data.remote.CavaResponse

/**
 * Detalle técnico de los sensores por cada sección de la cava.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaDetailScreen(cavas: List<CavaResponse>, onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F100D)).padding(32.dp)) {
        Text("Estado de la cava — detalle por sección", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            val sections = cavas.flatMap { it.secciones }
            sections.forEach { seccion ->
                CavaSectionCard(
                    title = "Sección ${seccion.nombre}",
                    temp = "${seccion.temperatura}°C",
                    hum = "${seccion.humedad.toInt()}%",
                    bottles = "${seccion.botellasActuales} botellas",
                    status = if(seccion.estado != "OPTIMO") "Revisar" else "Óptima",
                    statusColor = if(seccion.estado != "OPTIMO") Color(0xFFF9A825) else Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Tarjeta con métricas granulares de sensores. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaSectionCard(title: String, temp: String, hum: String, bottles: String, status: String, statusColor: Color, modifier: Modifier) {
    Surface(
        onClick = {},
        modifier = modifier.height(350.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1A1C18), focusedContainerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(32.dp))
            Text("Temperatura: $temp", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFB4F391))
            Text("Humedad: $hum", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4FC3F7))
            Spacer(Modifier.weight(1f))
            Text(bottles, color = Color.Gray)
        }
    }
}
```

### `MainTvScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/MainTvScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*

/** Orquestador de pantallas para la TV. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainTvScreen(viewModel: TvViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(TvScreen.DASHBOARD) }

    LaunchedEffect(uiState) {
        if (uiState is TvUiState.NotLinked) currentScreen = TvScreen.PAIRING
        else if (uiState is TvUiState.Linked && currentScreen == TvScreen.PAIRING) currentScreen = TvScreen.DASHBOARD
    }

    if (currentScreen != TvScreen.PAIRING && currentScreen != TvScreen.DASHBOARD) {
        BackHandler { currentScreen = TvScreen.DASHBOARD }
    }

    when (val state = uiState) {
        is TvUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFFB4F391)) }
        is TvUiState.NotLinked -> PairingScreen(state.pairingCode)
        is TvUiState.Linked -> {
            when (currentScreen) {
                TvScreen.DASHBOARD -> TvDashboardScreen(state.cavas, onNavigateToCavaDetail = { currentScreen = TvScreen.CAVA_DETAIL }, onNavigateToActivities = { currentScreen = TvScreen.ACTIVITIES }, onLogout = { viewModel.desvincularTv() })
                TvScreen.CAVA_DETAIL -> CavaDetailScreen(state.cavas, onNavigateBack = { currentScreen = TvScreen.DASHBOARD })
                TvScreen.ACTIVITIES -> ActivitiesScreen()
                else -> {}
            }
        }
        else -> {}
    }
}

enum class TvScreen { PAIRING, DASHBOARD, CAVA_DETAIL, ACTIVITIES }
```

### `PairingScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/PairingScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap

/** Pantalla que muestra el QR y código de vinculación. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PairingScreen(pairingCode: String) {
    val qr = remember(pairingCode) { QrGenerator.generateQrBitmap(pairingCode, 400) }
    Column(modifier = Modifier.fillMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Vincular Smart TV", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(32.dp)) {
            Box(Modifier.size(220.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(10.dp)) {
                qr?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR") }
            }
            Spacer(Modifier.width(48.dp))
            Column {
                Text("Código de vinculación:", color = Color.Gray)
                Text(pairingCode, style = MaterialTheme.typography.displayMedium, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

### `QrGenerator.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/QrGenerator.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Generador de bitmaps QR. */
object QrGenerator {
    /** Crea bitmap QR. */
    fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) for (y in 0 until size) bmp.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            bmp
        } catch (e: Exception) { null }
    }
}
```

### `TvViewModel.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/TvViewModel.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mx.utng.ecoviedos.data.remote.*
import mx.utng.ecoviedos.shared.data.mqtt.MqttManager

/** Sealed class para el estado de la UI de TV. */
sealed class TvUiState {
    data object Loading : TvUiState()
    data class NotLinked(val pairingCode: String) : TvUiState()
    data class Linked(val cavas: List<CavaResponse>) : TvUiState()
    data class Error(val message: String) : TvUiState()
}

/** ViewModel central de la TV. */
class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvUiState>(TvUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var mqtt: MqttManager? = null

    init { startPairingProcess() }

    private fun startPairingProcess() {
        viewModelScope.launch {
            while (true) {
                val res = RetrofitClient.tvService.checkStatus("tv_device_id")
                if (res.isSuccessful && res.body()?.isLinked == true) {
                    cargarCavas()
                    break
                }
                delay(5000)
            }
        }
    }

    private fun cargarCavas() {
        viewModelScope.launch {
            val res = RetrofitClient.cavaService.obtenerCavas()
            if (res.isSuccessful) _uiState.value = TvUiState.Linked(res.body() ?: emptyList())
        }
    }

    fun desvincularTv() {
        mqtt?.disconnect()
        _uiState.value = TvUiState.Loading
        startPairingProcess()
    }
}
```

---

## Flujo de Funcionamiento
1.  **Arranque:** La TV genera un ID único y pide un código al servidor.
2.  **Visualización:** El usuario ve el QR y el código en pantalla.
3.  **Vínculo:** El administrador autoriza desde la App móvil.
4.  **Monitoreo:** El `TvViewModel` recibe la señal de éxito, descarga las cavas e inicia la escucha MQTT para actualizar la UI en vivo.

## Ejecución
1.  Ejecutar módulo `:tv` en un emulador Android TV.
2.  Acceso a red obligatorio.
