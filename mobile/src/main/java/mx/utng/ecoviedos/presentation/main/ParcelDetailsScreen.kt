package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.MuestraResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelDetailsScreen(
    parcelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRegisterSample: (String) -> Unit,
    mainViewModel: MainViewModel,
    muestraViewModel: MuestraViewModel = viewModel(),
    userRol: String = ""
) {
    val parcelas by mainViewModel.parcelas.collectAsState()
    val parcela = remember(parcelId, parcelas) { parcelas.find { it.id == parcelId } }
    val uiState by muestraViewModel.uiState.collectAsState()
    
    val ultimaMuestra = (uiState as? MuestraUiState.Success)?.historial?.firstOrNull()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker && parcela != null) {
        val calendar = Calendar.getInstance()
        parcela.fechaCosecha?.let { calendar.time = it }
        
        android.app.DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                mainViewModel.actualizarFechaCosecha(parcela, newDate)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

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
                HarvestEstimateCard(
                    fecha = parcela.fechaCosecha,
                    indice = parcela.indiceMaduracion,
                    onScheduleClick = { showDatePicker = true }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RealTimeCard("Humedad", "${parcela.humedad.toInt()}%", Icons.Default.WaterDrop, Color(0xFF4FC3F7), Modifier.weight(1f))
                    RealTimeCard("Temp", "${parcela.temperatura.toInt()}°C", Icons.Default.Thermostat, Color(0xFFFF8A65), Modifier.weight(1f))
                    RealTimeCard("Suelo", "${parcela.humedadSuelo.toInt()}%", Icons.Default.Waves, Color(0xFF81C784), Modifier.weight(1f))
                }

                Text("Última Muestra de Campo", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SampleStatCard("Brix", ultimaMuestra?.brix?.toString() ?: parcela.brix?.toString() ?: "-", Modifier.weight(1f))
                    SampleStatCard("Acidez", ultimaMuestra?.acidez?.toString() ?: parcela.acidez?.toString() ?: "-", Modifier.weight(1f))
                    SampleStatCard("pH Fruto", ultimaMuestra?.ph?.toString() ?: parcela.ph?.toString() ?: "-", Modifier.weight(1f))
                    SampleStatCard("pH Suelo", ultimaMuestra?.phSuelo?.toString() ?: parcela.phSuelo?.toString() ?: "-", Modifier.weight(1f))
                }

                Text("Historial de Brix", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (uiState is MuestraUiState.Success) {
                    val historial = (uiState as MuestraUiState.Success).historial
                    BrixHistoryChart(historial)
                } else if (uiState is MuestraUiState.Loading) {
                    CircularProgressIndicator(color = Color(0xFFB4F391))
                }

                Spacer(Modifier.height(8.dp))

                if (userRol == "superusuario" || userRol == "trabajador") {
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
}

@Composable
fun HarvestEstimateCard(fecha: Date?, indice: Float, onScheduleClick: () -> Unit) {
    val locale = LocalLocale.current.platformLocale
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (fecha != null) Color(0xFFE3F2FD) else Color(0xFF2A2D26)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (fecha != null) "Cosecha programada" else "Cosecha no programada",
                color = if (fecha != null) Color(0xFF0D47A1) else Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
            
            if (fecha != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fechaStr = SimpleDateFormat("dd MMM yyyy", locale).format(fecha)
                    Text(fechaStr, color = Color(0xFF0D47A1), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    
                    TextButton(onClick = onScheduleClick) {
                        Text("Reagendar", color = Color(0xFF1976D2))
                    }
                }
                
                val diff = fecha.time - System.currentTimeMillis()
                val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                
                val (proximityText, proximityColor) = when {
                    days < 0 -> "Cosecha pasada" to Color.Red
                    days == 0 -> "¡Hoy es la cosecha!" to Color(0xFF2E7D32)
                    days <= 7 -> "Próxima en $days días" to Color(0xFFF57C00)
                    else -> "$days días restantes" to Color(0xFF1976D2)
                }
                
                Text(proximityText, color = proximityColor, style = MaterialTheme.typography.bodySmall)
            } else {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onScheduleClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Programar fecha de cosecha")
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Si hay fecha, la barra indica progreso hacia la fecha
            // Si no hay fecha, podría seguir indicando índice de maduración o estar vacía
            val progress = if (fecha != null) {
                val start = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // Asumimos ciclo de 30 días para visualización
                val total = fecha.time - start
                val elapsed = System.currentTimeMillis() - start
                (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            } else {
                indice / 100f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (fecha != null) Color(0xFF1976D2) else Color(0xFFB4F391).copy(alpha = 0.5f),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (fecha != null) "Inicio" else "0%", fontSize = 10.sp, color = Color.Gray)
                if (fecha == null) Text("Brix ${ indice.toInt() }°", fontSize = 10.sp, color = Color.Gray)
                Text(if (fecha != null) "Cosecha" else "100%", fontSize = 10.sp, color = Color.Gray)
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

    val locale = LocalLocale.current.platformLocale
    val chartData = muestras.take(7).sortedBy { it.fecha ?: it.createdAt }
    val maxBrix = (chartData.maxOfOrNull { it.brix } ?: 25.0).toFloat().coerceAtLeast(10f)
    
    val dateFormat = SimpleDateFormat("dd/MM", locale)

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = if (chartData.size > 1) width / (chartData.size - 1) else width

                    val path = Path()
                    val fillPath = Path()

                    chartData.forEachIndexed { index, muestra ->
                        val x = index * spacing
                        val brixValue = muestra.brix.toFloat()
                        val y = height - (brixValue / maxBrix * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == chartData.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    if (chartData.size > 1) {
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1976D2).copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )
                        drawPath(
                            path = path,
                            color = Color(0xFF4FC3F7),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    chartData.forEachIndexed { index, muestra ->
                        val x = index * spacing
                        val y = height - (muestra.brix.toFloat() / maxBrix * height)
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color(0xFF1976D2),
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartData.forEach { muestra ->
                    val date = try {
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)
                        isoFormat.parse(muestra.fecha ?: muestra.createdAt ?: "") ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }
                    Text(
                        text = dateFormat.format(date),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.width(35.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
