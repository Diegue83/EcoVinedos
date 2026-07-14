package mx.utng.ecoviedos.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material3.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BitacoraScreen(
    viewModel: BitacoraViewModel,
    idParcela: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val parcela = uiState.parcelas.find { it.id == idParcela }
    val listState = rememberScalingLazyListState()
    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

    // Animación para el punto rojo de grabación
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BITÁCORA",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB4F391)
                    )
                    Text(
                        text = parcela?.nombreParcela ?: "Parcela $idParcela",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRecording) Color.Red else Color(0xFF384B2F)
                    ),
                    onClick = {
                        if (uiState.isRecording) viewModel.stopRecording(context)
                        else viewModel.startRecording(context,context.filesDir) // Usar filesDir para persistencia
                    }
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Grabar",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .alpha(if (uiState.isRecording) alpha else 1f)
                            .background(if (uiState.isRecording) Color.Red else Color.Gray, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(uiState.recordedTime, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (uiState.bitacoras.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("REGISTROS LOCALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                items(uiState.bitacoras) { nota ->
                    Card(
                        onClick = { 
                            if (nota.path != null) viewModel.playAudio(nota.path)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (nota.path != null) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null, 
                                    tint = Color(0xFFB4F391),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column {
                                Text(
                                    text = nota.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = dateFormat.format(Date(nota.lastModified())),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
