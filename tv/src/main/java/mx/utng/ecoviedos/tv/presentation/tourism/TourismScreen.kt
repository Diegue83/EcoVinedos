package mx.utng.ecoviedos.tv.presentation.tourism

import androidx.compose.foundation.layout.*
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.data.remote.EventoResponse
import mx.utng.ecoviedos.presentation.admin.TourismViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TourismScreen(
    viewModel: TourismViewModel = viewModel()
) {
    val allEvents by viewModel.eventos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val attractions = remember(allEvents) {
        allEvents.filter { it.tipo == "TOURISM" }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarEventos("TOURISM")
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            text = "Turismo y Experiencias",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (attractions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay atracciones disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(attractions) { attraction ->
                    AttractionCard(attraction)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AttractionCard(attraction: EventoResponse) {
    Card(
        onClick = { /* Ver detalles de la atracción */ },
        modifier = Modifier.width(300.dp).height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Text(text = attraction.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = attraction.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
