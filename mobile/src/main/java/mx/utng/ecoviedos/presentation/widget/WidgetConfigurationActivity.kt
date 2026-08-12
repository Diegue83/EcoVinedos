package mx.utng.ecoviedos.presentation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANTE: Los parámetros de Glance vienen en los extras del Intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Reintentar obtenerlo si viene de una acción de Glance
            appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) 
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF81C784),
                    surface = Color(0xFF1E1E1E)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ParcelSelectorScreen(
                        onConfigFinished = { parcela, transparencia, tiempo ->
                            saveSelectionAndFinish(parcela, transparencia, tiempo)
                        }
                    )
                }
            }
        }
    }

    private fun saveSelectionAndFinish(parcela: Parcela, transparencia: Float, tiempo: Int) {
        lifecycleScope.launch {
            try {
                val glanceId = GlanceAppWidgetManager(this@WidgetConfigurationActivity)
                    .getGlanceIdBy(appWidgetId)
                
                updateAppWidgetState(this@WidgetConfigurationActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("parcel_id")] = parcela.id
                        this[stringPreferencesKey("parcel_name")] = parcela.nombreParcela
                        this[floatPreferencesKey("transparency")] = transparencia
                        this[intPreferencesKey("riego_time")] = tiempo
                    }
                }
                
                ParcelaGlanceWidget().update(this@WidgetConfigurationActivity, glanceId)

                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
            } catch (e: Exception) {
                setResult(Activity.RESULT_CANCELED)
            } finally {
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelSelectorScreen(onConfigFinished: (Parcela, Float, Int) -> Unit) {
    var parcelas by remember { mutableStateOf<List<Parcela>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedParcela by remember { mutableStateOf<Parcela?>(null) }
    var transparency by remember { mutableFloatStateOf(0.7f) }
    var riegoTime by remember { mutableIntStateOf(5) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.token.first()
        if (token != null) {
            val repo = ParcelaRepository()
            repo.obtenerParcelas(token).onSuccess {
                parcelas = it
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Configurar Widget", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text("1. Selecciona una Parcela", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(parcelas) { parcela ->
                        val isSelected = selectedParcela?.id == parcela.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedParcela = parcela },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(parcela.nombreParcela, fontWeight = FontWeight.Bold)
                                    Text(parcela.variedad, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                Text("2. Ajustes Visuales", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Text("Transparencia: ${(transparency * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = transparency,
                    onValueChange = { transparency = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )

                Text("Tiempo de Riego: $riegoTime min", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = riegoTime.toFloat(),
                    onValueChange = { riegoTime = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 59
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { selectedParcela?.let { onConfigFinished(it, transparency, riegoTime) } },
                    enabled = selectedParcela != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar Configuración", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
