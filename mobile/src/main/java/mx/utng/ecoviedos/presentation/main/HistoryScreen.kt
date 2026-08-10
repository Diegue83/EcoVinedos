package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.HistorialSensorResponse
import mx.utng.ecoviedos.data.remote.ResumenDiarioResponse
import mx.utng.ecoviedos.domain.model.Parcela
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    parcelas: List<Parcela>,
    viewModel: HistorialViewModel = viewModel()
) {
    var selectedParcela by remember { mutableStateOf(parcelas.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reciente (15m)", "Diario (1 año)")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(selectedParcela) {
        selectedParcela?.let { viewModel.cargarDatos(it.id) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de Sensores", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Parcela
        Box {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(selectedParcela?.nombreParcela ?: "Seleccionar parcela", color = Color.White)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFB4F391))
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF2A2D26))
            ) {
                parcelas.forEach { parcela ->
                    DropdownMenuItem(
                        text = { Text(parcela.nombreParcela, color = Color.White) },
                        onClick = {
                            selectedParcela = parcela
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFB4F391),
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is HistorialUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB4F391))
                is HistorialUiState.Success -> {
                    if (tabIndex == 0) {
                        RecentHistoryList(state.historial)
                    } else {
                        DailySummaryList(state.resumen)
                    }
                }
                is HistorialUiState.Error -> Text(state.mensaje, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecciona una parcela para ver los datos", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun RecentHistoryList(historial: List<HistorialSensorResponse>) {
    if (historial.isEmpty()) {
        EmptyState("No hay datos recientes")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(historial) { item ->
                HistoryItemCard(
                    fecha = item.fecha,
                    hAire = item.humedadAire,
                    temp = item.temperaturaAire,
                    hSuelo = item.humedadSuelo
                )
            }
        }
    }
}

@Composable
fun DailySummaryList(resumen: List<ResumenDiarioResponse>) {
    if (resumen.isEmpty()) {
        EmptyState("No hay resúmenes diarios")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(resumen) { item ->
                HistoryItemCard(
                    fecha = item.fecha,
                    hAire = item.humedadAirePromedio,
                    temp = item.temperaturaAirePromedio,
                    hSuelo = item.humedadSueloPromedio,
                    isSummary = true
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(fecha: String, hAire: Double, temp: Double, hSuelo: Double, isSummary: Boolean = false) {
    val locale = LocalLocale.current.platformLocale
    val date = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)
        isoFormat.parse(fecha) ?: Date()
    } catch (e: Exception) { Date() }
    
    val displayFormat = if (isSummary) SimpleDateFormat("dd MMM yyyy", locale) 
                        else SimpleDateFormat("dd MMM, HH:mm", locale)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(displayFormat.format(date), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(if (isSummary) "Promedio diario" else "Lectura sensor", fontSize = 11.sp, color = Color.Gray)
            }
            
            Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatValue("T°", "${temp.toInt()}°", Color(0xFFFF8A65))
                StatValue("H.A.", "${hAire.toInt()}%", Color(0xFF4FC3F7))
                StatValue("H.S.", "${hSuelo.toInt()}%", Color(0xFF81C784))
            }
        }
    }
}

@Composable
fun StatValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun EmptyState(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(msg, color = Color.Gray)
        }
    }
}
