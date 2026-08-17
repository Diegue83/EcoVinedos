package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourismManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    tourismViewModel: TourismViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
    showBackButton: Boolean = true
) {
    val events by tourismViewModel.eventos.collectAsState()
    val isLoading by tourismViewModel.isLoading.collectAsState()
    val token by mainViewModel.sessionToken.collectAsState(initial = "")

    LaunchedEffect(Unit) {
        tourismViewModel.cargarEventos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Actividades", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
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
                onClick = onNavigateToAdd,
                containerColor = Color(0xFFB4F391)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFB4F391))
                }
            } else if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay elementos registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(events) { event ->
                        EventAdminCard(
                            event = event,
                            onEdit = { onNavigateToEdit(event._id) },
                            onDelete = { 
                                token?.let { tourismViewModel.eliminarEvento(it, event._id) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventAdminCard(
    event: EventoResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D26))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.titulo,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (event.tipo == "TOURISM") "Atracción" else "Evento",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB4F391)
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFFB4AB))
            }
        }
    }
}
