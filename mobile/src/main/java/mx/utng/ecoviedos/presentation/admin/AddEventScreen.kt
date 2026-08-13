package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit,
    eventId: String? = null,
    tourismViewModel: TourismViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EVENT") }
    var cupo by remember { mutableStateOf("0") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    
    val isEdit = eventId != null
    val token by mainViewModel.sessionToken.collectAsState(initial = "")
    val isLoading by tourismViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Elemento" else "Nuevo Elemento", fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción Detallada") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cupo,
                    onValueChange = { cupo = it },
                    label = { Text("Cupo/Capacidad") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación") },
                    modifier = Modifier.weight(1.5f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de Imagen (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Tipo de Elemento", color = Color.White, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = type == "EVENT",
                    onClick = { type = "EVENT" },
                    label = { Text("Evento") },
                    enabled = !isLoading
                )
                FilterChip(
                    selected = type == "TOURISM",
                    onClick = { type = "TOURISM" },
                    label = { Text("Atracción") },
                    enabled = !isLoading
                )
                FilterChip(
                    selected = type == "NOTICE",
                    onClick = { type = "NOTICE" },
                    label = { Text("Aviso") },
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    token?.let {
                        // Assuming createEvento updated in ViewModel
                        tourismViewModel.crearEvento(it, title, description, type) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black),
                enabled = title.isNotBlank() && description.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
