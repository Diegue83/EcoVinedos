package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoviedos.domain.model.Parcela

@Composable
fun IrrigationScreen(
    parcelas: List<Parcela>,
    viewModel: MainViewModel
) {
    var selectedModo by remember { mutableStateOf("MANUAL") } // Default Manual
    var selectedDuracion by remember { mutableIntStateOf(10) }

    // Filtrar por el tipo de riego de la parcela y ordenar por prioridad
    val filteredParcelas = remember(parcelas, selectedModo) {
        parcelas.filter { it.tipoRiego == selectedModo }
               .sortedBy { it.humedadSuelo }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Riego", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE2E3DE))
            
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = selectedModo == "MANUAL",
                    onClick = { selectedModo = "MANUAL" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Manual")
                }
                SegmentedButton(
                    selected = selectedModo == "AUTO",
                    onClick = { selectedModo = "AUTO" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Auto")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Duración programada (minutos): $selectedDuracion", color = Color.White, fontSize = 14.sp)
        Slider(
            value = selectedDuracion.toFloat(),
            onValueChange = { selectedDuracion = it.toInt() },
            valueRange = 1f..60f,
            steps = 59,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFB4F391),
                activeTrackColor = Color(0xFFB4F391)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de consumo hídrico basada en déficit real
        val totalDeficit = filteredParcelas.sumOf { 
            maxOf(0f, it.umbralHumedadSuelo - it.humedadSuelo).toInt()
        }
        val waterNeededLiters = totalDeficit * 15 

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1D2024))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF7CB9FF))
                        Spacer(Modifier.width(8.dp))
                        Text("Déficit hídrico ($selectedModo)", fontSize = 14.sp)
                    }
                    Text("${waterNeededLiters} L", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7CB9FF))
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (totalDeficit / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color(0xFF7CB9FF),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Humedad de suelo actual vs Umbral mín.", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "ORDEN DE PRIORIDAD (${selectedModo})", 
            style = MaterialTheme.typography.labelLarge, 
            color = Color(0xFFB4F391),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredParcelas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay parcelas con válvula $selectedModo", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredParcelas) { parcela ->
                    val isUrgent = parcela.humedadSuelo < parcela.umbralHumedadSuelo
                    val statusText = if (parcela.riegoActivo) {
                        if (parcela.tiempoRestanteRiego >= 0) {
                            "Riego: ${parcela.tiempoRestanteRiego / 60}m ${parcela.tiempoRestanteRiego % 60}s restantes"
                        } else {
                            val overTime = kotlin.math.abs(parcela.tiempoRestanteRiego)
                            "¡Riego Excedido!: ${overTime / 60}m ${overTime % 60}s"
                        }
                    } else {
                        "Suelo ${parcela.humedadSuelo.toInt()}% - Mín ${parcela.umbralHumedadSuelo.toInt()}%"
                    }

                    IrrigationM3Item(
                        name = parcela.nombreParcela,
                        status = statusText,
                        badge = if (parcela.riegoActivo) "Activo" else if (isUrgent) "Crítico" else "Óptimo",
                        color = if (parcela.riegoActivo) Color(0xFF7CB9FF) else if (isUrgent) Color(0xFFFFB4AB) else Color(0xFFB4F391),
                        onColor = if (parcela.riegoActivo) Color(0xFF003258) else if (isUrgent) Color(0xFF690005) else Color(0xFF00390A),
                        isManualMode = true, 
                        riegoActivo = parcela.riegoActivo,
                        onToggle = {
                            viewModel.toggleRiego(
                                parcelId = parcela.id,
                                activo = !parcela.riegoActivo,
                                duracionMinutos = selectedDuracion,
                                modo = parcela.tipoRiego
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IrrigationM3Item(
    name: String,
    status: String,
    badge: String,
    color: Color,
    onColor: Color,
    isManualMode: Boolean,
    riegoActivo: Boolean,
    onToggle: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(status, fontSize = 12.sp, color = if (riegoActivo) Color(0xFF7CB9FF) else Color.Gray)
                }
            }
            
            if (isManualMode) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (riegoActivo) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                        contentDescription = if (riegoActivo) "Detener" else "Iniciar",
                        tint = if (riegoActivo) Color(0xFFFFB4AB) else Color(0xFFB4F391),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Badge(
                containerColor = color,
                contentColor = onColor,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}
