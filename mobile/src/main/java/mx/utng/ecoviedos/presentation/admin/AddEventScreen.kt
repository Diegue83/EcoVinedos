package mx.utng.ecoviedos.presentation.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.EventoRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.presentation.main.MainViewModel
import mx.utng.ecoviedos.utils.UriPathHelper

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
    var precio by remember { mutableStateOf("0") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEdit = eventId != null
    val token by mainViewModel.sessionToken.collectAsState(initial = "")
    val events by tourismViewModel.eventos.collectAsState()
    val isLoading by tourismViewModel.isLoading.collectAsState()
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(eventId, events) {
        if (isEdit && events.isNotEmpty()) {
            events.find { it._id == eventId }?.let { event ->
                title = event.titulo
                description = event.descripcion
                type = event.tipo
                cupo = event.cupo.toString()
                precio = event.precio.toString()
                imageUrl = event.imagenUrl ?: ""
                location = event.ubicacion ?: ""
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isUploading = true
                try {
                    val part = UriPathHelper.prepareMultipart(context, it, "image")
                    if (part != null) {
                        val response = RetrofitClient.uploadService.uploadImage("Bearer $token", part)
                        if (response.isSuccessful) {
                            imageUrl = response.body()?.imageUrl ?: ""
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar Actividad" else "Nueva Actividad", fontWeight = FontWeight.Bold) },
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
            // Selector de Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2D26))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color(0xFFB4F391))
                } else if (imageUrl.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Subir Imagen", color = Color.Gray)
                    }
                } else {
                    Text("Imagen cargada con éxito", color = Color(0xFFB4F391))
                    // Aquí podrías usar Coil para mostrar la previa
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (MXN)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = cupo,
                    onValueChange = { cupo = it },
                    label = { Text("Cupo") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación / Punto de encuentro") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391)),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Categoría", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                    label = { Text("Turismo") },
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    token?.let {
                        coroutineScope.launch {
                            try {
                                val request = EventoRequest(
                                    titulo = title,
                                    descripcion = description,
                                    tipo = type,
                                    precio = precio.toDoubleOrNull() ?: 0.0,
                                    cupo = cupo.toIntOrNull() ?: 0,
                                    imagenUrl = imageUrl,
                                    ubicacion = location
                                )
                                if (isEdit) {
                                    tourismViewModel.actualizarEvento(it, eventId!!, request) {
                                        onNavigateBack()
                                    }
                                } else {
                                    tourismViewModel.crearEvento(it, request) {
                                        onNavigateBack()
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black),
                enabled = title.isNotBlank() && description.isNotBlank() && !isLoading && !isUploading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEdit) "Guardar Cambios" else "Publicar Actividad", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
