package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import mx.utng.ecoviedos.data.remote.NotificacionResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificacionViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by mainViewModel.sessionToken.collectAsState(initial = "")

    LaunchedEffect(token) {
        token?.let { viewModel.cargarNotificaciones(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is NotificacionUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB4F391))
                is NotificacionUiState.Success -> {
                    if (state.notificaciones.isEmpty()) {
                        Text("No tienes notificaciones", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.notificaciones) { notif ->
                                NotificationItem(
                                    notif = notif, 
                                    onRead = { token?.let { viewModel.cambiarEstado(it, notif._id, "leida") } },
                                    onDiscard = { token?.let { viewModel.cambiarEstado(it, notif._id, "descartada") } }
                                )
                            }
                        }
                    }
                }
                is NotificacionUiState.Error -> Text(state.mensaje, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun NotificationItem(notif: NotificacionResponse, onRead: () -> Unit, onDiscard: () -> Unit) {
    val isRead = notif.estado == "leida"
    val icon = when (notif.tipo) {
        "humedad" -> Icons.Default.WaterDrop
        "cosecha" -> Icons.Default.Grass
        "desconexion" -> Icons.Default.WifiOff
        else -> Icons.Default.Notifications
    }

    val color = when (notif.tipo) {
        "humedad" -> Color(0xFF4FC3F7)
        "cosecha" -> Color(0xFF81C784)
        "desconexion" -> Color(0xFFFF8A65)
        else -> Color(0xFFB4F391)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isRead) Color.Transparent else Color.White.copy(alpha = 0.05f))
            .clickable { if (!isRead) onRead() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.titulo, fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(notif.mensaje, color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
                
                val date = try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    isoFormat.parse(notif.fecha) ?: Date()
                } catch (e: Exception) { Date() }
                
                Text(
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date),
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isRead) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFB4F391), CircleShape).padding(end = 8.dp))
                }
                IconButton(onClick = onDiscard) {
                    Icon(Icons.Default.Clear, contentDescription = "Descartar", tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.Gray.copy(alpha = 0.1f))
    }
}
