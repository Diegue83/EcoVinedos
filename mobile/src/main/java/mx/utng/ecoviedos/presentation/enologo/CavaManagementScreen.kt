package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.CavaResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CavaManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLinkSensor: (String, String) -> Unit,
    mainViewModel: MainViewModel = viewModel()
) {
    var cavas by remember { mutableStateOf<List<CavaResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val token by mainViewModel.sessionToken.collectAsState(initial = "")

    val cargarCavas = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.cavaService.obtenerCavas()
                if (response.isSuccessful) {
                    cavas = response.body() ?: emptyList()
                }
            } catch (e: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarCavas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cavas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1C18), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Diálogo para nueva sección */ }, containerColor = Color(0xFFB4F391)) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Sección")
            }
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cavas) { cava ->
                        CavaManageCard(
                            cava = cava,
                            token = token ?: "",
                            onLinkSensor = { onNavigateToLinkSensor(cava._id, cava.nombre) },
                            onUpdate = { cargarCavas() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CavaManageCard(cava: CavaResponse, token: String, onLinkSensor: () -> Unit, onUpdate: () -> Unit) {
    var bottles by remember { mutableStateOf(cava.botellasActuales.toString()) }
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color(0xFFB4F391))
                Spacer(Modifier.width(12.dp))
                Text(text = cava.nombre, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = bottles,
                    onValueChange = { bottles = it },
                    label = { Text("Número de Botellas") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391))
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { 
                        coroutineScope.launch {
                            try {
                                RetrofitClient.cavaService.actualizarBotellas(
                                    "Bearer $token",
                                    cava._id,
                                    mapOf("botellasActuales" to (bottles.toIntOrNull() ?: 0))
                                )
                                onUpdate()
                            } catch (e: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF384B2F))
                ) {
                    Text("Actualizar")
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onLinkSensor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (cava.sensorId == null) "Vincular Sensor BLE" else "Sensor: ${cava.sensorId}")
            }
        }
    }
}
