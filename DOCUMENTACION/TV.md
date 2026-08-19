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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
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
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

/**
 * Pantalla que muestra el carrusel horizontal de actividades y experiencias.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: TourismViewModel = viewModel()
) {
    val activities by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEventos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(32.dp)
    ) {
        Text(
            text = "Actividades y experiencias",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
            }
        } else if (activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay actividades programadas", color = Color.Gray)
            }
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(end = 32.dp)
            ) {
                items(activities.size) { index ->
                    val activity = activities[index]
                    ActivityCard(
                        title = activity.titulo,
                        desc = activity.descripcion,
                        price = "${activity.precio} MXN",
                        tag = if(activity.tipo == "TOURISM") "Turismo" else "Evento",
                        imageUrl = activity.imagenUrl,
                        bgColor = if(activity.tipo == "TOURISM") Color(0xFF2E7D32) else Color(0xFF1565C0),
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
fun ActivityCard(
    title: String,
    desc: String,
    price: String,
    tag: String,
    imageUrl: String?,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {},
        modifier = modifier.fillMaxHeight(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bgColor.copy(alpha = 0.2f),
            focusedContainerColor = bgColor.copy(alpha = 0.4f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de imagen si existe
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📋", fontSize = 32.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = desc, style = MaterialTheme.typography.titleMedium, color = Color.White, lineHeight = 28.sp, maxLines = 4)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(text = price, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tag, 
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFB4F391),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
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
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import mx.utng.ecoviedos.data.remote.CavaResponse

/**
 * Detalle técnico de los sensores por cada sección de la cava.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaDetailScreen(
    cavas: List<CavaResponse>,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Estado de la cava — detalle por sección",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text("En vivo", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB4F391))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val allSections = cavas.flatMap { it.secciones }
            if (allSections.isEmpty()) {
                Text("Cargando detalles de secciones...", color = Color.Gray)
            } else {
                allSections.forEach { seccion ->
                    val isWarning = seccion.estado != "OPTIMO"
                    CavaSectionCard(
                        title = "Sección ${seccion.nombre}",
                        temp = "${seccion.temperatura}°C",
                        hum = "${seccion.humedad.toInt()}%",
                        bottles = "${seccion.botellasActuales} botellas",
                        status = if(isWarning) "Revisar" else "Óptima",
                        statusColor = if(isWarning) Color(0xFFF9A825) else Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                        isWarning = isWarning
                    )
                }
            }
        }
    }
}

/** Tarjeta con métricas granulares de sensores. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaSectionCard(
    title: String,
    temp: String,
    hum: String,
    bottles: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Surface(
        onClick = {},
        modifier = modifier.height(350.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF1A1C18),
            focusedContainerColor = Color(0xFF2A2D26)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, if (isWarning) Color.Red else Color(0xFF3897F0))),
            border = if (isWarning) Border(border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))) else Border.None
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isWarning) Color.Red else Color.White)
            Spacer(Modifier.height(32.dp))
            
            StatDetail("Temperatura", temp, "Rango 14-18°C", if (isWarning) Color.Red else Color(0xFFB4F391))
            Spacer(Modifier.height(24.dp))
            StatDetail("Humedad", hum, "Rango 70-80%", if (isWarning) Color.Red else Color(0xFF4FC3F7))
            
            Spacer(Modifier.weight(1f))
            
            Text(text = bottles, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(
                text = status, 
                style = MaterialTheme.typography.labelSmall, 
                color = statusColor,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/** Detalle de métrica individual. */
@Composable
fun StatDetail(label: String, value: String, range: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.headlineLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = range, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.5f))
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*

/** Pantallas disponibles en la TV. */
enum class TvScreen {
    PAIRING,
    DASHBOARD,
    CAVA_DETAIL,
    ACTIVITIES
}

/** Orquestador de pantallas para la TV. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainTvScreen(
    viewModel: TvViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(TvScreen.DASHBOARD) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TvUiState.NotLinked -> currentScreen = TvScreen.PAIRING
            is TvUiState.Linked -> {
                if (currentScreen == TvScreen.PAIRING) {
                    currentScreen = TvScreen.DASHBOARD
                }
            }
            else -> {}
        }
    }

    // Manejo del botón Atrás del control remoto
    if (currentScreen != TvScreen.PAIRING && currentScreen != TvScreen.DASHBOARD) {
        BackHandler {
            currentScreen = TvScreen.DASHBOARD
        }
    }

    when (val state = uiState) {
        is TvUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
            }
        }
        is TvUiState.NotLinked -> {
            PairingScreen(pairingCode = state.pairingCode)
        }
        is TvUiState.Linked -> {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    TvScreen.DASHBOARD -> TvDashboardScreen(
                        cavas = state.cavas,
                        onNavigateToCavaDetail = { currentScreen = TvScreen.CAVA_DETAIL },
                        onNavigateToActivities = { currentScreen = TvScreen.ACTIVITIES },
                        onLogout = { viewModel.desvincularTv() }
                    )
                    TvScreen.CAVA_DETAIL -> CavaDetailScreen(
                        cavas = state.cavas,
                        onNavigateBack = { currentScreen = TvScreen.DASHBOARD }
                    )
                    TvScreen.ACTIVITIES -> ActivitiesScreen()
                    else -> {}
                }
            }
        }
        is TvUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}
```

### `PairingScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/PairingScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap

/** Pantalla que muestra el QR y código de vinculación. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PairingScreen(pairingCode: String) {
    val qrBitmap = remember(pairingCode) {
        QrGenerator.generateQrBitmap(pairingCode, 400)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vincular Smart TV al sistema",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Escanea el código QR o ingresa el código en la app del administrador",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // QR Code
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Pairing Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.width(48.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "Código de vinculación", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color(0xFF2A2D26)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = pairingCode.chunked(2).joinToString(" - "),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB4F391),
                        letterSpacing = 4.sp
                    )
                }
                Text(
                    text = "⏳ Válido por 15 minutos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pasos de vinculación
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepItem(1, "Abre la app del administrador en tu teléfono")
            StepItem(2, "Ve a Configuración -> Vincular TV")
            StepItem(3, "Ingresa el código o escanea el QR")
        }

        Text(
            text = "El panel se activará automáticamente al vincular",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

/** Elemento de paso en el proceso de vinculación. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StepItem(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF3897F0)),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyLarge, 
            color = Color.White,
            lineHeight = 24.sp
        )
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

/**
 * Generador de códigos QR para facilitar la vinculación de dispositivos.
 */
object QrGenerator {
    /**
     * Crea un objeto [Bitmap] que contiene el código QR del texto proporcionado.
     *
     * @param content Texto a codificar (normalmente el Pairing Code).
     * @param size Dimensiones del bitmap cuadrado.
     * @return Bitmap con el QR generado o null en caso de error.
     */
    fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
```

### `TvDashboardScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/TvDashboardScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import mx.utng.ecoviedos.data.remote.CavaResponse

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Dashboard central de monitoreo para TV.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDashboardScreen(
    cavas: List<CavaResponse>,
    onNavigateToCavaDetail: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onLogout: () -> Unit,
    tourismViewModel: TourismViewModel = viewModel()
) {
    val currentTime = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    val eventos by tourismViewModel.eventos.collectAsState()

    // Focus management
    val focusRequesterCava = remember { FocusRequester() }
    val focusRequesterActivities = remember { FocusRequester() }
    val focusRequesterLogout = remember { FocusRequester() }
    var lastFocusedCard by remember { mutableStateOf("cava") }

    LaunchedEffect(Unit) {
        if (lastFocusedCard == "cava") {
            focusRequesterCava.requestFocus()
        } else {
            focusRequesterActivities.requestFocus()
        }
    }

    // Estadísticas globales
    val todasLasSecciones = cavas.flatMap { it.secciones }
    val avgTemp = if (todasLasSecciones.isNotEmpty()) todasLasSecciones.map { it.temperatura }.average() else 0.0
    val avgHum = if (todasLasSecciones.isNotEmpty()) todasLasSecciones.map { it.humedad }.average() else 0.0
    val totalBottles = todasLasSecciones.sumOf { it.botellasActuales }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Eco-Viñedos Dolores — Temporada 2026",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(text = "Dashboard Principal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.colors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .focusRequester(focusRequesterLogout),
                    border = ButtonDefaults.border(
                        focusedBorder = Border(border = BorderStroke(2.dp, Color.Red))
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Desvincular")
                    Spacer(Modifier.width(8.dp))
                    Text("Desvincular")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), RoundedCornerShape(50)))
                        Spacer(Modifier.width(8.dp))
                        Text("En vivo", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB4F391))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(text = currentTime.value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Métrica principales
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Temp. promedio cava", "${String.format("%.1f", avgTemp)}°C", "Normal", Color(0xFF3897F0), Modifier.weight(1f))
            StatCard("Humedad promedio", "${String.format("%.0f", avgHum)}%", "Normal", Color(0xFF4FC3F7), Modifier.weight(1f))
            StatCard("Botellas en cava", "$totalBottles", "Total secciones", Color(0xFFF9A825), Modifier.weight(1f))
            StatCard("Visitas hoy", "14", "+3 reservas", Color(0xFF4CAF50), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Área principal interactiva
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Surface(
                onClick = { 
                    lastFocusedCard = "cava"
                    onNavigateToCavaDetail() 
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .focusRequester(focusRequesterCava)
                    .onFocusChanged { if(it.isFocused) lastFocusedCard = "cava" },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color(0xFF1A1C18),
                    focusedContainerColor = Color(0xFF2A2D26)
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(border = BorderStroke(2.dp, Color(0xFF3897F0)))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Estado de la Bodega", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF3897F0), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    if (cavas.isEmpty()) {
                        Text("No hay datos de cava", color = Color.Gray)
                    } else {
                        todasLasSecciones.take(5).forEach { seccion ->
                            CavaItem(
                                seccion.nombre, 
                                if(seccion.estado == "OPTIMO") "Óptimo" else "Revisar", 
                                if(seccion.estado == "OPTIMO") Color(0xFF4CAF50) else Color(0xFFF9A825)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text("Maduración por variedad", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    
                    VarietyProgress("Merlot", 0.85f, "82°Bx", Color(0xFF3897F0))
                    VarietyProgress("Viognier", 0.70f, "71°Bx", Color(0xFF4CAF50))
                    VarietyProgress("Gamacha", 0.65f, "68°Bx", Color(0xFFF9A825))
                }
            }

            Surface(
                onClick = { 
                    lastFocusedCard = "activities"
                    onNavigateToActivities() 
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.5f)
                    .focusRequester(focusRequesterActivities)
                    .onFocusChanged { if(it.isFocused) lastFocusedCard = "activities" },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color(0xFF1A1C18),
                    focusedContainerColor = Color(0xFF2A2D26)
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(border = BorderStroke(2.dp, Color(0xFF3897F0)))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Actividades y Experiencias", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    if (eventos.isEmpty()) {
                        PromotionCardSummary("Cargando eventos...", "", Color(0xFF1565C0))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            eventos.take(3).forEach { evento ->
                                PromotionCardSummary(evento.titulo, "$${evento.precio} MXN", if(evento.tipo == "TOURISM") Color(0xFF2E7D32) else Color(0xFF1565C0))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de métrica estadística. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatCard(label: String, value: String, subValue: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2D26))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = accentColor, fontWeight = FontWeight.Bold)
            Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.7f))
        }
    }
}

/** Fila informativa de sección de cava. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaItem(name: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text(
            text = status, 
            style = MaterialTheme.typography.labelLarge, 
            color = statusColor,
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/** Progreso de maduración por variedad de uva. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VarietyProgress(name: String, progress: Float, label: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, style = MaterialTheme.typography.labelLarge, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(50))) {
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(color, RoundedCornerShape(50)))
        }
    }
}

/** Resumen de tarjeta promocional de eventos. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PromotionCardSummary(title: String, price: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium, 
                color = Color.White, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = price, style = MaterialTheme.typography.labelLarge, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
        }
    }
}
```

### `TvViewModel.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/TvViewModel.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import mx.utng.ecoviedos.shared.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.remote.PairCodeRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.data.remote.SeccionCavaResponse

/** Sealed class para el estado de la UI de TV. */
sealed class TvUiState {
    data object Loading : TvUiState()
    data class NotLinked(val pairingCode: String) : TvUiState()
    data class Linked(val cavas: List<mx.utng.ecoviedos.data.remote.CavaResponse>) : TvUiState()
    data class Error(val message: String) : TvUiState()
}

/** ViewModel central de la TV. */
class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvUiState>(TvUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var pairingJob: Job? = null
    private var mqttManager: MqttManager? = null

    private val deviceId: String = android.provider.Settings.Secure.getString(
        application.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    ) ?: "tv_emulator_id"

    init {
        startPairingProcess()
    }

    /** Inicia ciclo de polling para verificar si el administrador vinculó este dispositivo. */
    private fun startPairingProcess() {
        pairingJob?.cancel()
        pairingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.tvService.checkStatus(deviceId)
                    if (response.isSuccessful) {
                        val session = response.body()
                        if (session?.isLinked == true) {
                            cargarDatosCava()
                            break // Detener este bucle al estar vinculado
                        } else if (session != null) {
                            _uiState.value = TvUiState.NotLinked(session.pairingCode)
                        }
                    } else if (response.code() == 404) {
                        getNewPairingCode()
                    } else {
                        _uiState.value = TvUiState.Error("Servidor: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _uiState.value = TvUiState.Error("Error de conexión: ${e.localizedMessage}")
                }
                delay(5000)
            }
        }
    }

    /** Inicializa la conexión MQTT compartida. */
    private fun initializeMqtt() {
        mqttManager?.disconnect()
        mqttManager = MqttManager(
            context = getApplication(),
            onMessageReceived = { id, hum, temp, _, _, _ ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarSeccionEnTiempoReal(id, hum, temp)
                }
            },
            onRiegoStatusReceived = { _, _, _ -> },
            onParcelListReceived = { },
            onCavaListReceived = { payload ->
                viewModelScope.launch(Dispatchers.Main) {
                    actualizarListaCavasMqtt(payload)
                }
            },
            onConnectionStatusChanged = { _, _ -> }
        )
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
        }
    }

    /** Actualiza una sección individual de cava tras recibir telemetría. */
    private fun actualizarSeccionEnTiempoReal(id: String, hum: Float, temp: Float) {
        val state = _uiState.value
        if (state is TvUiState.Linked) {
            var changed = false
            val updatedCavas = state.cavas.map { cava ->
                val index = cava.secciones.indexOfFirst { it._id == id }
                if (index != -1) {
                    changed = true
                    val updatedSecciones = cava.secciones.toMutableList()
                    updatedSecciones[index] = updatedSecciones[index].copy(
                        humedad = hum.toDouble(),
                        temperatura = temp.toDouble(),
                        ultimaLectura = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                    )
                    cava.copy(secciones = updatedSecciones)
                } else {
                    cava
                }
            }
            if (changed) {
                _uiState.value = TvUiState.Linked(updatedCavas)
            }
        }
    }

    /** Procesa actualización masiva de cavas vía MQTT. */
    private fun actualizarListaCavasMqtt(payload: String) {
        val state = _uiState.value
        if (state is TvUiState.Linked) {
            try {
                val type = object : TypeToken<List<SeccionCavaResponse>>() {}.type
                val list = Gson().fromJson<List<SeccionCavaResponse>>(payload, type)
                
                val updatedCavas = state.cavas.map { cava ->
                    val seccionesActualizadas = cava.secciones.map { seccion ->
                        list.find { it._id == seccion._id } ?: seccion
                    }
                    cava.copy(secciones = seccionesActualizadas)
                }
                _uiState.value = TvUiState.Linked(updatedCavas)
            } catch (e: Exception) {
                Log.e("TvViewModel", "Error parseando lista cavas MQTT", e)
            }
        }
    }

    /** Descarga datos estructurales de la bodega. */
    private fun cargarDatosCava() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TvUiState.Linked(response.body()!!)
                    initializeMqtt()
                } else {
                    _uiState.value = TvUiState.Error("Error al cargar cavas: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TvUiState.Error("Error al cargar cavas: ${e.localizedMessage}")
            }
        }
    }

    /** Solicita nuevo código si el anterior expiró. */
    private suspend fun getNewPairingCode() {
        try {
            val response = RetrofitClient.tvService.getPairingCode(PairCodeRequest(deviceId))
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = TvUiState.NotLinked(response.body()!!.pairingCode)
            } else {
                _uiState.value = TvUiState.Error("Código: ${response.code()}")
            }
        } catch (e: Exception) {
            _uiState.value = TvUiState.Error("Error al obtener código: ${e.localizedMessage}")
        }
    }

    /** Desvincula la TV y reinicia el flujo de pairing. */
    fun desvincularTv() {
        viewModelScope.launch {
            try {
                _uiState.value = TvUiState.Loading
                mqttManager?.disconnect()
                mqttManager = null
                RetrofitClient.tvService.unlinkTV(PairCodeRequest(deviceId))
                startPairingProcess()
            } catch (e: Exception) {
                Log.e("TvViewModel", "Error al desvincular", e)
                startPairingProcess()
            }
        }
    }

    /** Intenta reconectar con el servidor. */
    fun retry() {
        _uiState.value = TvUiState.Loading
        startPairingProcess()
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
    }
}
```

### `EventsScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/events/EventsScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation.events

import androidx.compose.foundation.layout.*
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

/** Pantalla de listado de eventos programados. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: TourismViewModel = viewModel()
) {
    val allEvents by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val events = remember(allEvents) {
        allEvents.filter { it.tipo == "EVENT" }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarEventos("EVENT")
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            text = "Eventos del Viñedo",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay eventos próximos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(events) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

/** Tarjeta individual de evento. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventCard(event: EventoResponse) {
    Card(
        onClick = { },
        modifier = Modifier.width(300.dp).height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Text(text = event.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = event.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
```

### `TourismScreen.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/presentation/tourism/TourismScreen.kt`
```kotlin
package mx.utng.ecoviedos.tv.presentation.tourism

import androidx.compose.foundation.layout.*
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

/** Pantalla de experiencias turísticas. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TourismScreen(
    viewModel: TourismViewModel = viewModel()
) {
    val allEvents by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val attractions = remember(allEvents) {
        allEvents.filter { it.tipo == "TOURISM" }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarEventos("TOURISM")
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            text = "Turismo y Experiencias",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (attractions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay atracciones disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(attractions) { attraction ->
                    AttractionCard(attraction)
                }
            }
        }
    }
}

/** Tarjeta individual de atracción turística. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AttractionCard(attraction: EventoResponse) {
    Card(
        onClick = { },
        modifier = Modifier.width(300.dp).height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Text(text = attraction.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = attraction.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
```

### `Color.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/ui/theme/Color.kt`
```kotlin
package mx.utng.ecoviedos.tv.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val VineyardLightGreen = Color(0xFFB4F391)
val DarkBackground = Color(0xFF1A1C18)
```

### `Theme.kt`
Ubicación: `tv/src/main/java/mx/utng/ecoviedos/tv/ui/theme/Theme.kt`
```kotlin
package mx.utng.ecoviedos.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorScheme = darkColorScheme(
    primary = VineyardLightGreen,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkBackground
)

/** Definición del tema visual para TV. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EcoViñedosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
```
