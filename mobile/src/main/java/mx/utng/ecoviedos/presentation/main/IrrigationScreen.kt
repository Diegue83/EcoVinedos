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
fun IrrigationScreen(parcelas: List<Parcela>) {
    var isAuto by remember { mutableStateOf(true) }

    // Ordenar por prioridad (humedad más baja primero)
    val sortedParcelas = parcelas.sortedBy { it.humedad }

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
            
            // Switch de modo M3
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = isAuto,
                    onClick = { isAuto = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Auto")
                }
                SegmentedButton(
                    selected = !isAuto,
                    onClick = { isAuto = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Manual")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de consumo hídrico (simulada con datos dinámicos)
        val totalWaterNeeded = parcelas.sumOf { (100 - it.humedad.toInt()) * 20 } // Estimación ficticia
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1D2024))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF7CB9FF))
                        Spacer(Modifier.width(8.dp))
                        Text("Déficit hídrico total", fontSize = 14.sp)
                    }
                    Text("${totalWaterNeeded} L", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7CB9FF))
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (totalWaterNeeded / 10000f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color(0xFF7CB9FF),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Estimación basada en humedad actual", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "ORDEN DE PRIORIDAD", 
            style = MaterialTheme.typography.labelLarge, 
            color = Color(0xFFB4F391),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(sortedParcelas) { parcela ->
                val isUrgent = parcela.humedad < parcela.umbralHumedad
                IrrigationM3Item(
                    name = parcela.nombreParcela, 
                    status = "Humedad ${parcela.humedad.toInt()}% - Umbral ${parcela.umbralHumedad.toInt()}%", 
                    badge = if (isUrgent) "Urgente" else "Normal", 
                    color = if (isUrgent) Color(0xFFFFB4AB) else Color(0xFFB4F391), 
                    onColor = if (isUrgent) Color(0xFF690005) else Color(0xFF00390A)
                )
            }
        }
    }
}

@Composable
fun IrrigationM3Item(name: String, status: String, badge: String, color: Color, onColor: Color) {
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
                    Text(status, fontSize = 12.sp, color = Color.Gray)
                }
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
