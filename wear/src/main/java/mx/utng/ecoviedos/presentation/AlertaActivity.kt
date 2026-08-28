package mx.utng.ecoviedos.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import mx.utng.ecoviedos.presentation.theme.AppTheme

class AlertaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mantener la pantalla encendida para la alerta
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                       android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                       android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val parcelaId = intent.getStringExtra("parcela_id") ?: ""
        val nombre = intent.getStringExtra("parcela") ?: "Parcela desconocida"
        val variedad = intent.getStringExtra("variedad") ?: ""
        val humedad = intent.getStringExtra("humedad") ?: "0%"

        setContent {
            AppTheme {
                AlertUI(
                    nombre = nombre,
                    variedad = variedad,
                    humedad = humedad,
                    onVerParcela = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            putExtra("navigate_to_parcel", parcelaId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    },
                    onCerrar = { finish() }
                )
            }
        }
    }
}

@Composable
fun AlertUI(
    nombre: String,
    variedad: String,
    humedad: String,
    onVerParcela: () -> Unit,
    onCerrar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Opacity,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "ALERTA URGENTE",
                color = Color.Red,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (variedad.isNotEmpty()) {
                Text(
                    text = variedad,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            Text(
                text = "Humedad: $humedad",
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCerrar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.size(42.dp)
                ) {
                    Text("X", fontSize = 14.sp)
                }

                Button(
                    onClick = onVerParcela,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Ver",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
