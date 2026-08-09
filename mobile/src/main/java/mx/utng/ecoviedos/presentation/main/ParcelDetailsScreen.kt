package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.MuestraResponse
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDetailsScreen(
    parcelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRegisterSample: (String) -> Unit,
    mainViewModel: MainViewModel,
    muestraViewModel: MuestraViewModel = viewModel()
) {
    val parcelas by mainViewModel.parcelas.collectAsState()
    val parcela = remember(parcelId, parcelas) { parcelas.find { it.id == parcelId } }
    val uiState by muestraViewModel.uiState.collectAsState()

    LaunchedEffect(parcelId) {
        muestraViewModel.cargarHistorial(parcelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parcela?.nombreParcela ?: "Detalle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        if (parcela == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Parcela no encontrada", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Cosecha Estimada
                HarvestEstimateCard(parcela.fechaCosecha, parcela.indiceMaduracion)

                // 2. Monitoreo en tiempo real (Humedad Aire, Temp, Humedad Suelo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RealTimeCard("Humedad", "${parcela.humedad.toInt()}%", Icons.Default.WaterDrop, Color(0xFF4FC3F7), Modifier.weight(1f))
                    RealTimeCard("Temp", "${parcela.temperatura.toInt()}°C", Icons.Default.Thermostat, Color(0xFFFF8A65), Modifier.weight(1f))
                    RealTimeCard("Suelo", "${parcela.humedadSuelo.toInt()}%", Icons.Default.Waves, Color(0xFF81C784), Modifier.weight(1f))
                }

                // 3. Última muestra
                Text("Última Muestra de Campo", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SampleStatCard("Brix", parcela.brix?.toString() ?: "-", Modifier.weight(1f))
                    SampleStatCard("Acidez", parcela.acidez?.toString() ?: "-", Modifier.weight(1f))
                    SampleStatCard("pH Suelo", parcela.phSuelo?.toString() ?: "-", Modifier.weight(1f))
                }

                // 4. Gráfica Historial Brix
                Text("Historial de Brix", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (uiState is MuestraUiState.Success) {
                    val historial = (uiState as MuestraUiState.Success).historial
                    BrixHistoryChart(historial)
                } else if (uiState is MuestraUiState.Loading) {
                    CircularProgressIndicator(color = Color(0xFFB4F391))
                }

                Spacer(Modifier.height(8.dp))

                // 5. Botón Registrar Muestra
                Button(
                    onClick = { onNavigateToRegisterSample(parcelId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar muestra de campo")
                }
            }
        }
    }
}

@Composable
fun HarvestEstimateCard(fecha: Date?, indice: Float) {
    val locale = LocalLocale.current.platformLocale
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cosecha estimada", color = Color(0xFF0D47A1), style = MaterialTheme.typography.labelMedium)
            val fechaStr = if (fecha != null) SimpleDateFormat("dd MMM yyyy", locale).format(fecha) else "Pendiente"
            Text(fechaStr, color = Color(0xFF0D47A1), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            if (fecha != null) {
                val diff = fecha.time - System.currentTimeMillis()
                val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                Text("$days días · confianza 88%", color = Color(0xFF1976D2), style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { indice },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF1976D2),
                trackColor = Color.White.copy(alpha = 0.5f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Inicio", fontSize = 10.sp, color = Color.Gray)
                Text("Brix ${ (indice * 100).toInt() }°", fontSize = 10.sp, color = Color.Gray)
                Text("Cosecha", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RealTimeCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SampleStatCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C18)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43493E))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB4F391))
        }
    }
}

@Composable
fun BrixHistoryChart(muestras: List<MuestraResponse>) {
    if (muestras.isEmpty()) {
        Text("No hay muestras registradas", color = Color.Gray, modifier = Modifier.padding(16.dp))
        return
    }

    val chartData = muestras.take(6).reversed() // Mostrar las últimas 6
    val maxBrix = (chartData.maxOfOrNull { it.brix } ?: 25.0).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            chartData.forEach { muestra ->
                val barHeight = (muestra.brix.toFloat() / maxBrix).coerceIn(0.1f, 1f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${muestra.brix.toInt()}°", fontSize = 10.sp, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .fillMaxHeight(barHeight)
                            .background(Color(0xFF1976D2), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
            }
        }
    }
}
