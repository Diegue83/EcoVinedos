package mx.utng.ecoviedos.presentation.admin

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    
    // Obtener SSID actual
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val currentSsid = remember { 
        val info = wifiManager.connectionInfo
        info.ssid.removeSurrounding("\"") 
    }
    
    var ssid by remember { mutableStateOf(if (currentSsid == "<unknown ssid>") "" else currentSsid) }
    var password by remember { mutableStateOf("") }
    val parcelas by viewModel.parcelas.collectAsState()
    var selectedParcela by remember { mutableStateOf<Parcela?>(null) }
    var isConfiguring by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Nodo IoT", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StepIndicator(1, "Hardware", step >= 1)
                StepIndicator(2, "Red", step >= 2)
                StepIndicator(3, "Vincular", step >= 3)
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (step) {
                1 -> ScanDevicesStep { device ->
                    selectedDevice = device
                    step = 2
                }
                2 -> WifiConfigStep(
                    ssid = ssid,
                    onSsidChange = { ssid = it },
                    password = password,
                    onPasswordChange = { password = it },
                    onNext = { step = 3 }
                )
                3 -> LinkParcelaStep(
                    parcelas = parcelas,
                    selectedParcela = selectedParcela,
                    onParcelaSelected = { selectedParcela = it },
                    onFinish = {
                        isConfiguring = true
                    }
                )
            }

            if (isConfiguring) {
                AlertDialog(
                    onDismissRequest = { isConfiguring = false },
                    title = { Text("Configurando...") },
                    text = { Text("Enviando configuración al nodo '$selectedDevice' para la parcela '${selectedParcela?.nombreParcela}'\nSSID: $ssid") },
                    confirmButton = {
                        TextButton(onClick = { 
                            isConfiguring = false
                            onNavigateBack()
                        }) {
                            Text("Aceptar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StepIndicator(num: Int, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = if (active) Color(0xFFB4F391) else Color.Gray,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num.toString(), color = if (active) Color.Black else Color.White)
            }
        }
        Text(label, color = if (active) Color.White else Color.Gray, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ScanDevicesStep(onDeviceSelected: (String) -> Unit) {
    var isScanning by remember { mutableStateOf(false) }
    val discoveredDevices = remember { mutableStateListOf<String>() } // Realmente vacío al inicio

    LaunchedEffect(Unit) {
        isScanning = true
        // Simulamos un escaneo real que no encuentra nada si no hay hardware
        kotlinx.coroutines.delay(3000)
        isScanning = false
    }
    
    Text("1. Escaneando dispositivos cercanos", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
    Spacer(modifier = Modifier.height(16.dp))
    
    if (isScanning) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
                Spacer(Modifier.height(16.dp))
                Text("Buscando placas de desarrollo...", color = Color.Gray)
            }
        }
    } else if (discoveredDevices.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1916))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFFF2B8B5), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "No se detectó hardware disponible",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF2B8B5),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Asegúrate de que la placa esté en modo emparejamiento y el Bluetooth de tu celular esté encendido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF2B8B5),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* Reiniciar escaneo */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B8B5), contentColor = Color(0xFF3D1916))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reintentar Escaneo")
                }
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(discoveredDevices) { device ->
                OutlinedCard(
                    onClick = { onDeviceSelected(device) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFFB4F391))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(device, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WifiConfigStep(
    ssid: String,
    onSsidChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Text("2. Configurar Red WiFi", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
        value = ssid,
        onValueChange = onSsidChange,
        label = { Text("Nombre de Red (SSID)") },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
        enabled = ssid.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
    ) {
        Text("Siguiente")
    }
}

@Composable
fun ColumnScope.LinkParcelaStep(
    parcelas: List<Parcela>,
    selectedParcela: Parcela?,
    onParcelaSelected: (Parcela) -> Unit,
    onFinish: () -> Unit
) {
    Text("3. Vincular a Parcela", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
    Spacer(modifier = Modifier.height(16.dp))
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
        items(parcelas) { parcela ->
            val isSelected = selectedParcela?.id == parcela.id
            OutlinedCard(
                onClick = { onParcelaSelected(parcela) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (isSelected) CardDefaults.outlinedCardColors(containerColor = Color(0xFF384B2F)) else CardDefaults.outlinedCardColors()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isSelected, onClick = { onParcelaSelected(parcela) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(parcela.nombreParcela, fontWeight = FontWeight.Bold)
                        Text(parcela.variedad, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = onFinish,
        modifier = Modifier.fillMaxWidth(),
        enabled = selectedParcela != null,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
    ) {
        Text("Finalizar Configuración")
    }
}
