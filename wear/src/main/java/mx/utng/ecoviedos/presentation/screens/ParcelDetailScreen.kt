package mx.utng.ecoviedos.presentation.screens

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
import androidx.wear.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow

@Composable
fun ParcelDetailScreen(
    viewModel: BitacoraViewModel,
    idParcela: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val parcela = uiState.parcelas.find { it.id == idParcela }

    val statusColor = when {
        parcela?.riegoActivo == true -> Color(0xFF1976D2) // Blue for irrigation
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
                    text = "${parcela?.humedadSuelo?.toInt() ?: 0}",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 44.sp,
                    color = Color.White
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = "HUMEDAD SUELO",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMiniCard(label = "Temp", value = "${parcela?.temperatura?.toInt() ?: 0}°C")
                DetailMiniCard(label = "Def.", value = "${maxOf(0f, (parcela?.umbralHumedadSuelo ?: 40f) - (parcela?.humedadSuelo ?: 0f)).toInt()}%")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (parcela != null) {
                Button(
                    onClick = { 
                        if (parcela.riegoActivo) {
                            viewModel.detenerRiego(parcela.id)
                        } else {
                            viewModel.activarRiego(parcela.id)
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (parcela.riegoActivo) Color.Red else Color(0xFFB4F391)
                    )
                ) {
                    Icon(
                        imageVector = if (parcela.riegoActivo) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (parcela.riegoActivo) "Detener" else "Iniciar",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
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
