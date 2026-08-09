package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoviedos.domain.model.Parcela

@Composable
fun MaturationContent(
    parcelas: List<Parcela>, 
    onNavigateToParcelDetails: (String) -> Unit,
    onRefresh: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Maduración",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Temporada 2024", color = Color.Gray, fontSize = 14.sp)
            }
            Surface(
                color = Color(0xFFD0E4FF).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    "${parcelas.size} parcelas",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0E4FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pestaña Actual activa
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Actual", color = Color(0xFFD0E4FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color(0xFFD0E4FF)))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(8.dp))

        // Cabecera de Tabla
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Variedad", modifier = Modifier.weight(1.5f), color = Color.Gray, fontSize = 12.sp)
            Text("Brix", modifier = Modifier.weight(0.7f), color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("pH", modifier = Modifier.weight(0.7f), color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("Acid.", modifier = Modifier.weight(0.7f), color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("Est.", modifier = Modifier.weight(1f), color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(parcelas) { parcela ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToParcelDetails(parcela.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        parcela.variedad.ifBlank { parcela.nombreParcela },
                        modifier = Modifier.weight(1.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Brix con color dinámico
                    val brixValue = parcela.brix ?: 0f
                    val brixColor = when {
                        brixValue >= 20 -> Color(0xFF4FC3F7)
                        brixValue >= 15 -> Color(0xFF81C784)
                        else -> Color(0xFFFFB74D)
                    }

                    Text(
                        if (parcela.brix != null) "%.1f".format(parcela.brix) else "-",
                        modifier = Modifier.weight(0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = brixColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        parcela.ph?.let { "%.2f".format(it) } ?: "-",
                        modifier = Modifier.weight(0.7f),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        parcela.acidez?.let { "%.1f".format(it) } ?: "-",
                        modifier = Modifier.weight(0.7f),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Madurez Estimada (Badge)
                    val progress = parcela.indiceMaduracion.toInt().coerceIn(0, 100)
                    val badgeColor = when {
                        progress > 90 -> Color(0xFFE3F2FD).copy(alpha = 0.2f)
                        progress > 80 -> Color(0xFFE8F5E9).copy(alpha = 0.2f)
                        else -> Color(0xFFFFF3E0).copy(alpha = 0.2f)
                    }
                    val textColor = when {
                        progress > 90 -> Color(0xFF4FC3F7)
                        progress > 80 -> Color(0xFF81C784)
                        else -> Color(0xFFFFB74D)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .background(badgeColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$progress%",
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.Gray.copy(alpha = 0.2f)))
            }
        }
    }
}
