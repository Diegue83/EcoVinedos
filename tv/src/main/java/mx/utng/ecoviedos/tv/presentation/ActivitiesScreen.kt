package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: TourismViewModel = viewModel()
) {
    val activities by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEventos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F100D))
            .padding(32.dp)
    ) {
        Text(
            text = "Actividades y experiencias",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB4F391))
            }
        } else if (activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay actividades programadas", color = Color.Gray)
            }
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(end = 32.dp)
            ) {
                items(activities.size) { index ->
                    val activity = activities[index]
                    ActivityCard(
                        title = activity.titulo,
                        desc = activity.descripcion,
                        price = "${activity.precio} MXN",
                        tag = if(activity.tipo == "TOURISM") "Turismo" else "Evento",
                        imageUrl = activity.imagenUrl,
                        bgColor = if(activity.tipo == "TOURISM") Color(0xFF2E7D32) else Color(0xFF1565C0),
                        modifier = Modifier.width(300.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActivityCard(
    title: String,
    desc: String,
    price: String,
    tag: String,
    imageUrl: String?,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {},
        modifier = modifier.fillMaxHeight(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bgColor.copy(alpha = 0.2f),
            focusedContainerColor = bgColor.copy(alpha = 0.4f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de imagen si existe
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📋", fontSize = 32.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = desc, style = MaterialTheme.typography.titleMedium, color = Color.White, lineHeight = 28.sp, maxLines = 4)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(text = price, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tag, 
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFB4F391),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
