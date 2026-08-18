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

import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PairingScreen(pairingCode: String) {
    val qrBitmap = remember(pairingCode) {
        QrGenerator.generateQrBitmap(pairingCode, 400)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
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
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // QR Code
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Pairing Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: CircularProgressIndicator()
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

        Spacer(modifier = Modifier.height(32.dp))

        // Steps
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepItem(1, "Abre la app del administrador en tu teléfono")
            StepItem(2, "Ve a Configuración -> Vincular TV")
            StepItem(3, "Ingresa el código o escanea el QR")
        }

        Text(
            text = "El panel se activará automáticamente al vincular",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StepItem(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF3897F0)),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyLarge, 
            color = Color.White,
            lineHeight = 24.sp
        )
    }
}
