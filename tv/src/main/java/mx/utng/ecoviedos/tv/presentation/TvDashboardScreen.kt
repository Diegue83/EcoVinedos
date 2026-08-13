package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDashboardScreen(
    cavas: List<CavaResponse>,
    onNavigateToCavaDetail: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onLogout: () -> Unit
) {
    val currentTime = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }

    val avgTemp = if (cavas.isNotEmpty()) cavas.map { it.temperatura }.average() else 0.0
    val avgHum = if (cavas.isNotEmpty()) cavas.map { it.humedad }.average() else 0.0
    val totalBottles = cavas.sumOf { it.botellasActuales }

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
                // Logout Button for TV
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.colors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    modifier = Modifier.padding(end = 16.dp)
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

        // Top Stats Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Temp. promedio cava", "${String.format("%.1f", avgTemp)}°C", "Normal", Color(0xFF3897F0), Modifier.weight(1f))
            StatCard("Humedad promedio", "${String.format("%.0f", avgHum)}%", "Normal", Color(0xFF4FC3F7), Modifier.weight(1f))
            StatCard("Botellas en cava", "$totalBottles", "Total secciones", Color(0xFFF9A825), Modifier.weight(1f))
            StatCard("Visitas hoy", "14", "+3 reservas", Color(0xFF4CAF50), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Content Area
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Surface(
                onClick = onNavigateToCavaDetail,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
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
                    Text("Estado de la Cava", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3897F0), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    if (cavas.isEmpty()) {
                        Text("No hay datos de cava", color = Color.Gray)
                    } else {
                        cavas.forEach { cava ->
                            CavaItem(cava.nombre, if(cava.estado == "OPTIMO") "Óptimo" else "Revisar", if(cava.estado == "OPTIMO") Color(0xFF4CAF50) else Color(0xFFF9A825))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text("Maduración por variedad", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    
                    VarietyProgress("Merlot", 0.85f, "82°Bx", Color(0xFF3897F0))
                    VarietyProgress("Viognier", 0.70f, "71°Bx", Color(0xFF4CAF50))
                    VarietyProgress("Gamacha", 0.65f, "68°Bx", Color(0xFFF9A825))
                }
            }

            Surface(
                onClick = onNavigateToActivities,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.5f),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF2A2D26).copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Actividades y Experiencias", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    PromotionCardSummary("Tour Harvest Experience", "$850 MXN", Color(0xFF1565C0))
                    Spacer(Modifier.height(12.dp))
                    PromotionCardSummary("Membresía Primavera", "Desde $680", Color(0xFF2E7D32))
                    Spacer(Modifier.height(12.dp))
                    PromotionCardSummary("Maridaje Privado", "$1,200 MXN", Color(0xFF5D4037))
                }
            }
        }
    }
}

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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaItem(name: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Text(
            text = status, 
            style = MaterialTheme.typography.labelSmall, 
            color = statusColor,
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VarietyProgress(name: String, progress: Float, label: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(50))) {
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(color, RoundedCornerShape(50)))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PromotionCardSummary(title: String, price: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = price, style = MaterialTheme.typography.labelSmall, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
        }
    }
}
