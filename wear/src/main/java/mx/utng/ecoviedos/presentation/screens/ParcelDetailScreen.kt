package mx.utng.ecoviedos.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun ParcelDetailScreen(
    viewModel: BitacoraViewModel,
    idParcela: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val parcela = uiState.parcelas.find { it.id == idParcela }

    val statusColor = when {
        parcela?.esHumedadCritica() == true -> Color(0xFFD32F2F)
        parcela?.activa == false -> Color.Gray
        else -> Color(0xFF2E7D32)
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = parcela?.nombreParcela?.uppercase() ?: "SIN SELECCIÓN",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB4F391),
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${parcela?.humedad?.toInt() ?: 0}",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 54.sp,
                    color = Color.White
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Text(
                text = "HUMEDAD ACTUAL",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailMiniCard(label = "Temp", value = "${parcela?.temperatura?.toInt() ?: 0}°C")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(18.dp)
                    .width(60.dp)
                    .background(statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (parcela?.esHumedadCritica() == true) "ALERTA" else "SISTEMA OK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DetailMiniCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
