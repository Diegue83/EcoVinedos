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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CavaStateScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de la Cava", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1C18), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF0F100D)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MobileStatCard("Temp. cava", "18°C", "Normal", Color(0xFF3897F0), Modifier.weight(1f))
                MobileStatCard("Humedad", "82%", "Normal", Color(0xFF4FC3F7), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            // Stats Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MobileStatCard("Botellas", "247", "3 variedades", Color(0xFFF9A825), Modifier.weight(1f))
                MobileStatCard("Visitas", "14", "+3 reservas", Color(0xFF4CAF50), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sections
            Text("Secciones", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            MobileCavaSection("Sección Roble", "Óptimo", Color(0xFF4CAF50))
            MobileCavaSection("Sección Acero", "Óptimo", Color(0xFF4CAF50))
            MobileCavaSection("Bodega privada", "Revisar", Color(0xFFF9A825))

            Spacer(modifier = Modifier.height(24.dp))

            // Variety Progress
            Text("Maduración por Variedad", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            MobileVarietyProgress("Merlot", 0.85f, "82°Bx", Color(0xFF3897F0))
            MobileVarietyProgress("Viognier", 0.70f, "71°Bx", Color(0xFF4CAF50))
            MobileVarietyProgress("Gamacha", 0.65f, "68°Bx", Color(0xFFF9A825))
        }
    }
}

@Composable
fun MobileStatCard(label: String, value: String, subValue: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = accentColor, fontWeight = FontWeight.Bold)
            Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun MobileCavaSection(name: String, status: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C18))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, color = Color.White)
            Text(
                text = status, 
                style = MaterialTheme.typography.labelSmall, 
                color = statusColor,
                modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)
            )
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
