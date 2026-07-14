package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParcelScreen(
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var variedad by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var umbralHumedad by remember { mutableStateOf("30") }
    var umbralTemp by remember { mutableStateOf("25") }

    val uiState by adminViewModel.uiState.collectAsState()
    val estaGuardando = uiState is AddParcelUiState.Loading

    // Navega de regreso solo cuando el backend confirma que se guardó
    LaunchedEffect(uiState) {
        if (uiState is AddParcelUiState.Success) {
            adminViewModel.resetState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Parcela", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (nombre.isNotBlank() && variedad.isNotBlank() && !estaGuardando) {
                        adminViewModel.addParcel(
                            nombre,
                            variedad,
                            area.toIntOrNull() ?: 0,
                            umbralHumedad.toFloatOrNull() ?: 30f,
                            umbralTemp.toFloatOrNull() ?: 25f
                        )
                    }
                },
                containerColor = Color(0xFFB4F391),
                contentColor = Color(0xFF1A1C18),
                icon = {
                    if (estaGuardando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
                text = { Text(if (estaGuardando) "Guardando..." else "Guardar Parcela") }
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Información General",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB4F391)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Parcela") },
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

            OutlinedTextField(
                value = variedad,
                onValueChange = { variedad = it },
                label = { Text("Variedad de Uva") },
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

            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Área (m²)") },
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Configuración de Umbrales",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB4F391)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = umbralHumedad,
                    onValueChange = { umbralHumedad = it },
                    label = { Text("Humedad Mín (%)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = umbralTemp,
                    onValueChange = { umbralTemp = it },
                    label = { Text("Temp Máx (°C)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Mensaje de error si la creación falla en el backend
            if (uiState is AddParcelUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2F2F).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (uiState as AddParcelUiState.Error).mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFFB4AB),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF384B2F).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB4F391))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Estos umbrales se usarán para generar alertas automáticas en el panel de control y en el reloj.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}