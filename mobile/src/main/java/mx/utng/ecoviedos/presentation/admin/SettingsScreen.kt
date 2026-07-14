package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel
) {
    var mqttIp by remember { mutableStateOf(viewModel.getMqttIp()) }
    val mqttStatus by viewModel.mqttStatus.collectAsState()
    val isConnected by viewModel.isMqttConnected.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }

    // Mostrar diálogo si hay un error crítico
    LaunchedEffect(mqttStatus) {
        if (!isConnected && mqttStatus.contains("Error", ignoreCase = true)) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Fallo de Conexión") },
            text = { Text("No se pudo conectar al servidor MQTT. Revisa la IP y asegúrate de estar en la misma red.\n\nDetalle: $mqttStatus") },
            confirmButton = {
                TextButton(onClick = { 
                    showErrorDialog = false
                    viewModel.updateMqttIp(mqttIp)
                }) { Text("Reintentar") }
            },
            dismissButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("Cerrar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Conexión con Mosquitto",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB4F391)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2D26)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estado:", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mqttStatus.length > 20) mqttStatus.take(17) + "..." else mqttStatus,
                        color = if (isConnected) Color(0xFFB4F391) else Color(0xFFF39191),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = mqttIp,
                onValueChange = { mqttIp = it },
                label = { Text("Dirección IP del Servidor") },
                placeholder = { Text("Ej: 192.168.1.75") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB4F391),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFB4F391),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = { 
                    viewModel.updateMqttIp(mqttIp)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB4F391),
                    contentColor = Color(0xFF1A1C18)
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar y Reconectar")
            }

            Text(
                "Nota: Asegúrate de que el teléfono y el servidor Mosquitto (Node-RED) estén en la misma red Wi-Fi.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
