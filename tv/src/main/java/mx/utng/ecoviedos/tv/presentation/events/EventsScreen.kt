package mx.utng.ecoviedos.tv.presentation.events

import androidx.compose.foundation.layout.*
import androidx.tv.foundation.lazy.grid.*
import androidx.tv.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.utng.ecoviedos.domain.model.VinedoEvent
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventsScreen() {
    val events = remember {
        listOf(
            VinedoEvent("1", "Vendimia 2026", "Gran fiesta de la cosecha", Date()),
            VinedoEvent("2", "Cata de Vinos", "Experiencia sensorial única", Date()),
            VinedoEvent("3", "Tour por el Viñedo", "Conoce nuestras instalaciones", Date()),
            VinedoEvent("4", "Cena Maridaje", "Lo mejor de nuestra cava", Date())
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            text = "Eventos del Viñedo",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(events) { event ->
                EventCard(event)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventCard(event: VinedoEvent) {
    Card(
        onClick = { /* Navegar a detalles */ },
        modifier = Modifier.width(300.dp).height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = event.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
