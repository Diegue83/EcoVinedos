package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.CavaResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CavaStateScreen(
    onNavigateBack: () -> Unit,
    enologoViewModel: EnologoViewModel = viewModel(),
    showBackButton: Boolean = true
) {
    val cavas by enologoViewModel.cavas.collectAsState()
    val isLoading by enologoViewModel.isLoading.collectAsState()

    val avgTemp = if (cavas.isNotEmpty()) cavas.map { it.temperatura }.average() else 0.0
    val avgHum = if (cavas.isNotEmpty()) cavas.map { it.humedad }.average() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de la Cava", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1C18), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF0F100D)
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
                // Average Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C18)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB4F391).copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Temp. Promedio", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("${String.format(java.util.Locale.US, "%.1f", avgTemp)}°C", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
                        }
                        VerticalDivider(modifier = Modifier.height(50.dp).width(1.dp), color = Color.Gray.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Humedad Promedio", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("${avgHum.toInt()}%", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Detail Sections
                Text("Detalle por Secciones", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                
                cavas.forEach { cava ->
                    MobileCavaSection(cava.nombre, "${cava.temperatura}°C", "${cava.humedad.toInt()}%", cava.estado)
                }

                Spacer(Modifier.height(8.dp))

                // Variety Progress (Based on average or specific data if available)
                Text("Maduración por Variedad", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                MobileVarietyProgress("Merlot", 0.85f, "82°Bx", Color(0xFF3897F0))
                MobileVarietyProgress("Viognier", 0.70f, "71°Bx", Color(0xFF4CAF50))
                MobileVarietyProgress("Gamacha", 0.65f, "68°Bx", Color(0xFFF9A825))
            }
        }
    }
}

@Composable
fun MobileCavaSection(name: String, temp: String, hum: String, status: String) {
    val statusColor = if(status == "OPTIMO") Color(0xFF4CAF50) else Color(0xFFF9A825)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = name, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if(status == "OPTIMO") "Óptimo" else "Revisar",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Temperatura", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(temp, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Humedad", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(hum, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MobileVarietyProgress(name: String, progress: Float, label: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            color = color,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )
    }
}
