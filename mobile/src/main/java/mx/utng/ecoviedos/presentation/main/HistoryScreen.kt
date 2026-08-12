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
    viewModel: HistorialViewModel
) {
    val selectedId by viewModel.selectedParcelId.collectAsState()
    val selectedParcela = remember(selectedId, parcelas) {
        parcelas.find { it.id == selectedId } ?: parcelas.firstOrNull()
    }
    
    var expanded by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reciente", "Diario")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        selectedId?.let { viewModel.cargarDatos(it) }
    }

    LaunchedEffect(parcelas) {
        if (selectedId == null && parcelas.isNotEmpty()) {
            viewModel.seleccionarParcela(parcelas.first().id)
        }
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
                            viewModel.seleccionarParcela(parcela.id)
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
                        RecentHistoryList(state.historial, state.riegos, selectedParcela)
                    } else {
                        DailySummaryList(state.resumen, state.riegos, selectedParcela)
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
fun RecentHistoryList(historial: List<HistorialSensorResponse>, riegos: List<mx.utng.ecoviedos.data.remote.RiegoResponse>, parcela: Parcela?) {
    if (historial.isEmpty()) {
        EmptyState("No hay datos recientes")
    } else {
        val locale = LocalLocale.current.platformLocale
        val groupedHistorial = remember(historial) {
            historial.groupBy { item ->
                try {
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale).parse(item.fecha) ?: Date()
                    SimpleDateFormat("dd MMM yyyy, hh a", locale).format(date)
                } catch (e: Exception) { "Desconocido" }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            groupedHistorial.forEach { (hourSection, items) ->
                item {
                    Text(
                        text = hourSection,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB4F391),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
                items(items) { item ->
                    // Verificar si hubo riego activo durante esta lectura
                    val itemDate = try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale).parse(item.fecha) ?: Date()
                    } catch (e: Exception) { Date() }

                    val isIrrigating = riegos.any { r ->
                        try {
                            val rStart = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale).parse(r.fecha ?: "") ?: Date()
                            val rEnd = Date(rStart.time + r.duracion * 60L * 1000L)
                            // Un margen de 15 minutos para atrapar la lectura del sensor
                            itemDate.time >= rStart.time && itemDate.time <= rEnd.time
                        } catch (e: Exception) { false }
                    }

                    val currentConsumption = if (isIrrigating) {
                        (parcela?.consumoAguaM2 ?: 3.0f).toDouble() * (parcela?.areaM2 ?: 1).toDouble()
                    } else 0.0

                    HistoryItemCard(
                        fecha = item.fecha,
                        hAire = item.humedadAire,
                        temp = item.temperaturaAire,
                        hSuelo = item.humedadSuelo,
                        aguaLiters = currentConsumption, // En este caso son L/h momentáneos
                        parcela = parcela
                    )
                }
            }
        }
    }
}

@Composable
fun DailySummaryList(resumen: List<ResumenDiarioResponse>, riegos: List<mx.utng.ecoviedos.data.remote.RiegoResponse>, parcela: Parcela?) {
    if (resumen.isEmpty()) {
        EmptyState("No hay resúmenes diarios")
    } else {
        val locale = LocalLocale.current.platformLocale
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(resumen) { item ->
                // Calcular consumo total para este día
                val dayStr = try {
                    val date = SimpleDateFormat("yyyy-MM-dd", locale).parse(item.fecha) ?: Date()
                    SimpleDateFormat("yyyy-MM-dd", locale).format(date)
                } catch (e: Exception) { "" }

                val totalDayLiters = riegos.filter { riego ->
                    try {
                        val rDate = SimpleDateFormat("yyyy-MM-dd", locale).parse(riego.fecha ?: "") ?: Date()
                        SimpleDateFormat("yyyy-MM-dd", locale).format(rDate) == dayStr
                    } catch (e: Exception) { false }
                }.sumOf { r ->
                    // Cálculo basado en la fórmula solicitada: (Duración / 60) * consumoAguaM2 * areaM2
                    val duracionHoras = r.duracion.toDouble() / 60.0
                    val consumoM2 = (parcela?.consumoAguaM2 ?: 3.0).toDouble()
                    val area = (parcela?.areaM2 ?: 1).toDouble()
                    (duracionHoras * consumoM2 * area).toInt()
                }

                HistoryItemCard(
                    fecha = item.fecha,
                    hAire = item.humedadAirePromedio,
                    temp = item.temperaturaAirePromedio,
                    hSuelo = item.humedadSueloPromedio,
                    aguaLiters = totalDayLiters.toDouble(),
                    isSummary = true,
                    parcela = parcela
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    fecha: String, 
    hAire: Double, 
    temp: Double, 
    hSuelo: Double, 
    aguaLiters: Double = 0.0,
    isSummary: Boolean = false,
    parcela: Parcela? = null
) {
    val locale = LocalLocale.current.platformLocale
    val date = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)
        isoFormat.parse(fecha) ?: Date()
    } catch (e: Exception) { Date() }
    
    val displayFormat = if (isSummary) SimpleDateFormat("dd MMM yyyy", locale) 
                        else SimpleDateFormat("h:mm a", locale)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(displayFormat.format(date), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(if (isSummary) "Promedio diario" else "Lectura sensor", fontSize = 11.sp, color = Color.Gray)
            }
            
            Row(modifier = Modifier.weight(2.2f), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatValue("T°", "${temp.toInt()}°", Color(0xFFFF8A65))
                StatValue("H.A.", "${hAire.toInt()}%", Color(0xFF4FC3F7))
                StatValue("H.S.", "${hSuelo.toInt()}%", Color(0xFF81C784))
                if (isSummary || aguaLiters > 0) {
                    StatValue("Agua", if (isSummary) "${aguaLiters.toInt()}L" else "${String.format(locale, "%.1f", aguaLiters)}L/h", Color(0xFF7CB9FF))
                }
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
