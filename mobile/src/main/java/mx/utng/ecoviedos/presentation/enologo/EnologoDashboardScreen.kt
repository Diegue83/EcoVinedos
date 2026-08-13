package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.CavaResponse
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnologoDashboardScreen(
    onLogout: () -> Unit,
    enologoViewModel: EnologoViewModel = viewModel(),
    tourismViewModel: TourismViewModel = viewModel()
) {
    // In real app, these come from ViewModels
    // val cavas by enologoViewModel.cavas.collectAsState()
    // val events by tourismViewModel.eventos.collectAsState()
    
    // For now, let's assume we have them or use mock for UI structure with "REAL" data logic
    val cavas = listOf(
        CavaResponse("1", "Roble", "ROBLE", 16.5, 75.0, 500, 104, null, "OPTIMO"),
        CavaResponse("2", "Acero", "ACERO", 17.2, 78.0, 500, 89, null, "OPTIMO"),
        CavaResponse("3", "Privada", "PRIVADA", 20.1, 81.0, 200, 54, null, "REVISAR")
    )
    
    val avgTemp = cavas.map { it.temperatura }.average()
    val totalBottles = cavas.sumOf { it.botellasActuales }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Eco-Viñedos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión", tint = Color(0xFFFFB4AB))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Resumen de Producción",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Estado actual de la cava y eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard("Temp. Media", "${String.format("%.1f", avgTemp)}°C", Color(0xFF3897F0), Modifier.weight(1f))
                DashboardStatCard("Total Botellas", "$totalBottles", Color(0xFFF9A825), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cava Sections Preview
            Text("Secciones de Cava", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            cavas.take(3).forEach { cava ->
                CavaPreviewItem(cava)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Events Preview
            Text("Próximas Actividades", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            // This would take from events list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vendimia 2026", fontWeight = FontWeight.Bold, color = Color(0xFFB4F391))
                    Text("12 de Octubre - 10:00 AM", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text("Quedan 15 lugares disponibles", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF1A1C18), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(cava.nombre, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${cava.botellasActuales} botellas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            text = if(cava.estado == "OPTIMO") "Óptimo" else "Revisar",
            color = if(cava.estado == "OPTIMO") Color(0xFF4CAF50) else Color(0xFFF9A825),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
