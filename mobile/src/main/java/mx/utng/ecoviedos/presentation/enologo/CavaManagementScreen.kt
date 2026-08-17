package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.CavaResponse
import mx.utng.ecoviedos.data.remote.SeccionCavaResponse
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CavaManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLinkSensor: (String, String) -> Unit,
    enologoViewModel: EnologoViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val cavas by enologoViewModel.cavas.collectAsState()
    val isLoading by enologoViewModel.isLoading.collectAsState()
    val token by mainViewModel.sessionToken.collectAsState(initial = "")

    LaunchedEffect(Unit) {
        enologoViewModel.cargarDatos()
    }

    var showAddCavaDialog by remember { mutableStateOf(false) }
    var selectedCavaForSection by remember { mutableStateOf<CavaResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Bodega", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { showAddCavaDialog = true }, 
                containerColor = Color(0xFFB4F391)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cava")
            }
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (isLoading && cavas.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(cavas) { cava ->
                        CavaGroupCard(
                            cava = cava,
                            token = token ?: "",
                            onAddSection = { selectedCavaForSection = cava },
                            onDeleteCava = { enologoViewModel.eliminarCava(token ?: "", cava._id) },
                            onLinkSensor = onNavigateToLinkSensor,
                            enologoViewModel = enologoViewModel
                        )
                    }
                }
            }
        }

        if (showAddCavaDialog) {
            AddCavaDialog(
                onDismiss = { showAddCavaDialog = false },
                onConfirm = { nombre, ubicacion ->
                    enologoViewModel.crearCava(token ?: "", nombre, ubicacion, "")
                    showAddCavaDialog = false
                }
            )
        }

        selectedCavaForSection?.let { cava ->
            AddSeccionDialog(
                cavaNombre = cava.nombre,
                onDismiss = { selectedCavaForSection = null },
                onConfirm = { nombre, tipo, capacidad ->
                    enologoViewModel.crearSeccion(token ?: "", cava._id, nombre, tipo, capacidad)
                    selectedCavaForSection = null
                }
            )
        }
    }
}

@Composable
fun CavaGroupCard(
    cava: CavaResponse,
    token: String,
    onAddSection: () -> Unit,
    onDeleteCava: () -> Unit,
    onLinkSensor: (String, String) -> Unit,
    enologoViewModel: EnologoViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23261E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB4F391).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFFB4F391))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = cava.nombre, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = cava.ubicacion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Row {
                    IconButton(onClick = onAddSection) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Añadir Sección", tint = Color(0xFFB4F391))
                    }
                    IconButton(onClick = onDeleteCava) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Cava", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
            
            if (cava.secciones.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))
                
                cava.secciones.forEach { seccion ->
                    SeccionManageItem(
                        seccion = seccion,
                        token = token,
                        onLinkSensor = { onLinkSensor(seccion._id, seccion.nombre) },
                        onDelete = { enologoViewModel.eliminarSeccion(token, seccion._id) },
                        enologoViewModel = enologoViewModel
                    )
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Text(
                    "Sin secciones registradas", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SeccionManageItem(
    seccion: SeccionCavaResponse, 
    token: String, 
    onLinkSensor: () -> Unit,
    onDelete: () -> Unit,
    enologoViewModel: EnologoViewModel
) {
    var bottles by remember { mutableStateOf(seccion.botellasActuales.toString()) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Sincronizar el estado local si el remoto cambia (ej. tras cargarDatos)
    LaunchedEffect(seccion.botellasActuales) {
        bottles = seccion.botellasActuales.toString()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = seccion.nombre, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Row {
                    IconButton(onClick = onLinkSensor, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Sensors, 
                            contentDescription = "Sensor", 
                            tint = if (seccion.sensorId != null) Color(0xFFB4F391) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = bottles,
                    onValueChange = { bottles = it },
                    label = { Text("Botellas", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White, 
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { 
                        isSaving = true
                        enologoViewModel.actualizarBotellas(
                            token = token, 
                            seccionId = seccion._id, 
                            cantidad = bottles.toIntOrNull() ?: 0,
                            onComplete = { isSaving = false }
                        )
                    },
                    enabled = !isSaving && token.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF384B2F)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCavaDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Cava / Bodega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                TextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, ubicacion) }, enabled = nombre.isNotBlank()) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddSeccionDialog(cavaNombre: String, onDismiss: () -> Unit, onConfirm: (String, String, Int) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("ROBLE") }
    var capacidad by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Sección a $cavaNombre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre de Sección") })
                // Simplificado: En un entorno real usaríamos un dropdown
                TextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (ROBLE, ACERO, PRIVADA)") })
                TextField(value = capacidad, onValueChange = { capacidad = it }, label = { Text("Capacidad (Botellas)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, tipo, capacidad.toIntOrNull() ?: 100) }, enabled = nombre.isNotBlank()) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
