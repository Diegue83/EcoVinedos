package mx.utng.ecoviedos.tv.presentation

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
fun ExperiencesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(32.dp)
    ) {
        Text(
            text = "Experiencias y promociones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExperienceCard(
                title = "Tour Harvest Experience",
                desc = "Recorrido por el viñedo con cata de 3 vinos seleccionados. Sábados y domingos 10am-2pm. Grupos máximo 12 personas.",
                price = "$850 MXN",
                tag = "Por persona",
                bgColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            ExperienceCard(
                title = "Membresía Primavera",
                desc = "6 botellas al mes con 20% de descuento permanente. Envío incluido en Querétaro. Cancela en cualquier momento.",
                price = "Desde $680 / mes",
                tag = "Suscripción",
                bgColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            ExperienceCard(
                title = "Maridaje Privado",
                desc = "Experiencia exclusiva de maridaje con el enólogo de la bodega. Viernes, sáb y dom. Máximo 8 personas por sesión.",
                price = "$1,200 MXN",
                tag = "Reserva requerida",
                bgColor = Color(0xFF5D4037),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ExperienceCard(
    title: String,
    desc: String,
    price: String,
    tag: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {},
        modifier = modifier.fillMaxHeight(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bgColor.copy(alpha = 0.8f),
            focusedContainerColor = bgColor
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍷", fontSize = 32.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f), lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(text = price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tag, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color(0xFFB4F391),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
