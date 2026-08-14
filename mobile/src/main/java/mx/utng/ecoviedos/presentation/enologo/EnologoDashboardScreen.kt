package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.CavaResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnologoDashboardScreen(
    onLogout: () -> Unit,
    enologoViewModel: EnologoViewModel = viewModel()
) {
    val cavas by enologoViewModel.cavas.collectAsState()
    val events by enologoViewModel.eventos.collectAsState()
    val isLoading by enologoViewModel.isLoading.collectAsState()
    
    val allSections = cavas.flatMap { it.secciones }
    val avgTemp = if (allSections.isNotEmpty()) allSections.map { it.temperatura }.average() else 0.0
    val totalBottles = allSections.sumOf { it.botellasActuales }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Eco-Viñedos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión", tint = Color(0xFFFFB4AB))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        if (isLoading && cavas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Resumen de Producción",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Quick Stats Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardStatCard("Temp. Media", "${String.format(Locale.US, "%.1f", avgTemp)}°C", Color(0xFF3897F0), Modifier.weight(1f))
                    DashboardStatCard("Total Botellas", "$totalBottles", Color(0xFFF9A825), Modifier.weight(1f))
                }

                // Cava Sections Preview
                Text("Secciones de Cava", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (cavas.isEmpty()) {
                    Text("No hay cavas registradas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                } else {
                    cavas.forEach { cava ->
                        CavaPreviewItem(cava)
                    }
                }

                // Upcoming Events Preview
                Text("Próximas Actividades", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (events.isEmpty()) {
                    Text("No hay actividades registradas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                } else {
                    events.take(3).forEach { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(event.titulo, fontWeight = FontWeight.Bold, color = Color(0xFFB4F391))
                                Text(event.fecha, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                Text("Capacidad: ${event.cupo} personas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CavaPreviewItem(cava: CavaResponse) {
    val totalBottles = cava.secciones.sumOf { it.botellasActuales }
    val isAnyWarning = cava.secciones.any { it.estado != "OPTIMO" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF2A2D26), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(cava.nombre, fontWeight = FontWeight.Bold, color = Color.White)
            Text("$totalBottles botellas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            text = if(!isAnyWarning) "Óptimo" else "Revisar",
            color = if(!isAnyWarning) Color(0xFF4CAF50) else Color(0xFFF9A825),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
