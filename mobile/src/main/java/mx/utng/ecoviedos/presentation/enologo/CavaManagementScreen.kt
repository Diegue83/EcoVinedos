package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CavaManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLinkSensor: (String, String) -> Unit // cavaId, cavaNombre
) {
    // Mock data for cavas (En producción vendrán de CavaService)
    val cavas = listOf(
        Pair("1", "Sección Roble"),
        Pair("2", "Sección Acero"),
        Pair("3", "Bodega Privada")
    )

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
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Vincular Sensores y Contar Botellas", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cavas) { cava ->
                    CavaManageCard(
                        id = cava.first,
                        name = cava.second,
                        onLinkSensor = { onNavigateToLinkSensor(cava.first, cava.second) }
                    )
                }
            }
        }
    }
}

@Composable
fun CavaManageCard(id: String, name: String, onLinkSensor: () -> Unit) {
    var bottles by remember { mutableStateOf("100") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color(0xFFB4F391))
                Spacer(Modifier.width(12.dp))
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = Color.White)
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
                Button(onClick = { /* Update bottle count via API */ }) {
                    Text("Guardar")
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onLinkSensor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Vincular Sensor BLE")
            }
        }
    }
}
