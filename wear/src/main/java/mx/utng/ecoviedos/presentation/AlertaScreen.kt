package mx.utng.ecoviedos.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun AlertaScreen(
    parcela: String = "Parcela 7",
    variedad: String = "Cabernet",
    humedad: String = "Humedad crítica"
) {

    var isRiegoActivado by remember { mutableStateOf(false) }
    var minutosRestantes by remember { mutableIntStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (!isRiegoActivado) {

            Text(
                text = "⚠️",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "¡Alerta!",
                color = Color.Red,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = humedad,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "$parcela - $variedad",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { isRiegoActivado = true },
                colors = ButtonDefaults.primaryButtonColors(
                    backgroundColor = Color.Red
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "ACTIVAR RIEGO",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

        } else {

            Text(
                text = "✅",
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "¡Riego activado!",
                color = Color(0xFF4CAF50),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "$parcela - $variedad",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Duración: $minutosRestantes min",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "320 L estimados",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

