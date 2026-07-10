package mx.utng.ecoviedos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard

@Composable
fun MyParcelsScreen(
    viewModel: BitacoraViewModel,
    onParcelClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedId by viewModel.selectedParcelId.collectAsState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 20.dp)
        ) {
            item {
                Text(
                    text = "MIS PARCELAS",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(uiState.parcelas) { parcela ->
                val isSelected = parcela.id == selectedId
                TitleCard(
                    onClick = { onParcelClick(parcela.id) },
                    title = {
                        Text(
                            text = "Parcela ${parcela.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFFB4F391) else Color.White
                        )
                    },
                    subtitle = {
                        Text(parcela.nombreParcela)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${parcela.humedad.toInt()}% Hum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (parcela.esHumedadCritica()) Color.Red else Color.LightGray
                        )
                        if (isSelected) {
                            Text("●", color = Color(0xFFB4F391))
                        }
                    }
                }
            }
        }
    }
}
