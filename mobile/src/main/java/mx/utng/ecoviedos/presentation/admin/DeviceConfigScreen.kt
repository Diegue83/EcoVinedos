package mx.utng.ecoviedos.presentation.admin

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConfigScreen(
    onNavigateBack: () -> Unit,
    mainViewModel: MainViewModel,
    configViewModel: DeviceConfigViewModel
) {
    val context = LocalContext.current
    val uiState by configViewModel.uiState.collectAsState()
    val discoveredDevices by configViewModel.discoveredDevices.collectAsState()
    val isBluetoothEnabled by configViewModel.isBluetoothEnabled.collectAsState()
    
    var step by remember { mutableIntStateOf(1) }
    var selectedDeviceName by remember { mutableStateOf("") }
    
    // Obtener SSID actual
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val currentSsid = remember { 
        val info = wifiManager.connectionInfo
        info.ssid.removeSurrounding("\"") 
    }
    
    var ssid by remember { mutableStateOf(if (currentSsid == "<unknown ssid>") "" else currentSsid) }
    var password by remember { mutableStateOf("") }
    val parcelas by mainViewModel.parcelas.collectAsState()
    var selectedParcela by remember { mutableStateOf<Parcela?>(null) }

    // Manejo de permisos
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            configViewModel.startScanning()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(bluetoothPermissions.toTypedArray())
        configViewModel.checkBluetoothStatus()
    }

    // Detener escaneo al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            configViewModel.stopScanning()
        }
    }

    // Navegación automática por estados de BLE
    LaunchedEffect(uiState) {
        when (uiState) {
            is BleUiState.Connected -> if (step == 1) step = 2
            is BleUiState.Success -> {
                // Diálogo de éxito ya manejado abajo
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Nodo IoT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        configViewModel.resetState()
                        onNavigateBack()
                    }) {
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
                1 -> ScanDevicesStep(
                    devices = discoveredDevices,
                    uiState = uiState,
                    isBluetoothEnabled = isBluetoothEnabled,
                    onDeviceSelected = { device ->
                        @SuppressLint("MissingPermission")
                        val name = device.name ?: "Desconocido"
                        selectedDeviceName = name
                        configViewModel.connectToDevice(device)
                    },
                    onRetry = { configViewModel.startScanning() }
                )
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
                        selectedParcela?.let {
                            configViewModel.sendConfig(ssid, password, it.id, it.nombreParcela)
                        }
                    }
                )
            }

            // Diálogos de Estado
            when (val state = uiState) {
                is BleUiState.Connecting -> LoadingDialog("Conectando con $selectedDeviceName...")
                is BleUiState.Sending -> LoadingDialog("Enviando configuración...")
                is BleUiState.VerifyingWiFi -> LoadingDialog(state.message)
                is BleUiState.Success -> {
                    AlertDialog(
                        onDismissRequest = { configViewModel.resetState(); onNavigateBack() },
                        title = { Text("Configuración Exitosa") },
                        text = { Text("El nodo '$selectedDeviceName' ha sido configurado y vinculado correctamente.") },
                        confirmButton = {
                            TextButton(onClick = { 
                                configViewModel.resetState()
                                onNavigateBack()
                            }) { Text("Finalizar") }
                        }
                    )
                }
                is BleUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = { configViewModel.resetState() },
                        title = { Text("Error") },
                        text = { Text(state.message) },
                        confirmButton = {
                            TextButton(onClick = { 
                                val wasWifiError = state.message.contains("WiFi", ignoreCase = true)
                                if (wasWifiError) {
                                    configViewModel.clearError()
                                    step = 2
                                } else {
                                    configViewModel.resetState()
                                    step = 1
                                }
                            }) { Text("Reintentar") }
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun LoadingDialog(message: String) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
                Spacer(Modifier.height(16.dp))
                Text(message, textAlign = TextAlign.Center)
            }
        }
    )
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
fun ScanDevicesStep(
    devices: List<BluetoothDevice>,
    uiState: BleUiState,
    isBluetoothEnabled: Boolean,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onRetry: () -> Unit
) {
    Text("1. Selecciona tu placa ESP32", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
    Spacer(modifier = Modifier.height(16.dp))
    
    if (!isBluetoothEnabled) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF410002))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = Color(0xFFF2B8B5))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Bluetooth apagado", fontWeight = FontWeight.Bold, color = Color(0xFFF2B8B5))
                    Text("Por favor, enciende el Bluetooth para buscar nodos IoT.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF2B8B5))
                }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        if (devices.isEmpty() && uiState !is BleUiState.Scanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1916))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = Color(0xFFF2B8B5), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No se detectó hardware", fontWeight = FontWeight.Bold, color = Color(0xFFF2B8B5))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B8B5), contentColor = Color(0xFF3D1916))) {
                        Text("Reintentar Escaneo")
                    }
                }
            }
        } else {
            Column {
                if (uiState is BleUiState.Scanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFFB4F391),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Buscando dispositivos...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices) { device ->
                        @SuppressLint("MissingPermission")
                        val name = device.name ?: "Dispositivo sin nombre"
                        OutlinedCard(
                            onClick = { onDeviceSelected(device) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFFB4F391))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold)
                                    Text(device.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
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
        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
        isError = ssid.isBlank(),
        supportingText = { if (ssid.isBlank()) Text("El SSID es obligatorio") }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña WiFi (Mín. 8 caracteres)") },
        modifier = Modifier.fillMaxWidth(),
        isError = password.isNotBlank() && password.length < 8,
        supportingText = { if (password.isNotBlank() && password.length < 8) Text("Contraseña demasiado corta") },
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
        enabled = ssid.isNotBlank() && password.length >= 8,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
    ) {
        Text("Continuar a Vinculación")
    }
}

@Composable
fun ColumnScope.LinkParcelaStep(
    parcelas: List<Parcela>,
    selectedParcela: Parcela?,
    onParcelaSelected: (Parcela) -> Unit,
    onFinish: () -> Unit
) {
    val availableParcelas = remember(parcelas) {
        parcelas.filter { it.nodoVinculado == null }
    }

    Text("3. Vincular a Parcela", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB4F391))
    Spacer(modifier = Modifier.height(16.dp))
    
    if (availableParcelas.isEmpty()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No hay parcelas disponibles para vincular", color = Color.Gray, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(availableParcelas) { parcela ->
                val isSelected = selectedParcela?.id == parcela.id
                OutlinedCard(
                    onClick = { onParcelaSelected(parcela) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isSelected) CardDefaults.outlinedCardColors(containerColor = Color(0xFF384B2F)) else CardDefaults.outlinedCardColors()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = onFinish,
        modifier = Modifier.fillMaxWidth(),
        enabled = selectedParcela != null,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
    ) {
        Text("Enviar Configuración al Nodo")
    }
}
