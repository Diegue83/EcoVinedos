package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PairingScreen(pairingCode: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vincular Smart TV al sistema",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Escanea el código QR o ingresa el código en la app del administrador",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // QR Code Placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Simplified QR representation
                Column {
                    Row { Box(Modifier.size(40.dp).background(Color.Black)); Box(Modifier.size(40.dp).background(Color.White)); Box(Modifier.size(40.dp).background(Color.Black)) }
                    Row { Box(Modifier.size(40.dp).background(Color.White)); Box(Modifier.size(40.dp).background(Color.Black)); Box(Modifier.size(40.dp).background(Color.White)) }
                    Row { Box(Modifier.size(40.dp).background(Color.Black)); Box(Modifier.size(40.dp).background(Color.White)); Box(Modifier.size(40.dp).background(Color.Black)) }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "Código de vinculación", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color(0xFF2A2D26)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = pairingCode.chunked(2).joinToString(" - "),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB4F391),
                        letterSpacing = 4.sp
                    )
                }
                Text(
                    text = "⏳ Válido por 15 minutos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Steps
        Column(modifier = Modifier.fillMaxWidth(0.6f)) {
            StepItem(1, "Abre la app del administrador en tu teléfono")
            StepItem(2, "Ve a Configuración -> Vincular TV")
            StepItem(3, "Ingresa el código o escanea el QR")
        }

        Text(
            text = "El panel se activará automáticamente al vincular",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StepItem(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF3897F0)),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}
