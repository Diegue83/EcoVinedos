package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaDetailScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Estado de la cava — detalle por sección",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            // Indicator "En vivo"
            Box(
                modifier = Modifier
                    .background(Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text("En vivo", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB4F391))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CavaSectionCard("Sección Roble", "16.5°C", "75%", "104 botellas", "Óptima", Color(0xFF4CAF50), Modifier.weight(1f))
            CavaSectionCard("Sección Acero", "17.2°C", "78%", "89 botellas", "Óptima", Color(0xFF4CAF50), Modifier.weight(1f))
            CavaSectionCard("Bodega privada", "20.1°C", "81%", "54 botellas", "Revisar", Color(0xFFF9A825), Modifier.weight(1f), isWarning = true)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CavaSectionCard(
    title: String,
    temp: String,
    hum: String,
    bottles: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Surface(
        onClick = {},
        modifier = modifier.height(350.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF1A1C18),
            focusedContainerColor = Color(0xFF2A2D26)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, if (isWarning) Color.Red else Color(0xFF3897F0))),
            border = if (isWarning) Border(border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))) else Border.None
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isWarning) Color.Red else Color.White)
            Spacer(Modifier.height(32.dp))
            
            StatDetail("Temperatura", temp, "Rango 14-18°C", if (isWarning) Color.Red else Color(0xFFB4F391))
            Spacer(Modifier.height(24.dp))
            StatDetail("Humedad", hum, "Rango 70-80%", if (isWarning) Color.Red else Color(0xFF4FC3F7))
            
            Spacer(Modifier.weight(1f))
            
            Text(text = bottles, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(
                text = status, 
                style = MaterialTheme.typography.labelSmall, 
                color = statusColor,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun StatDetail(label: String, value: String, range: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.headlineLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = range, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.5f))
    }
}
