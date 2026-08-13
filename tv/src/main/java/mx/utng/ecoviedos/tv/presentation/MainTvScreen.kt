package mx.utng.ecoviedos.tv.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import mx.utng.ecoviedos.tv.presentation.events.EventsScreen
import mx.utng.ecoviedos.tv.presentation.tourism.TourismScreen

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainTvScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Eventos", "Turismo")
    val icons = listOf(Icons.Default.CalendarToday, Icons.Default.Explore)

    Row(modifier = Modifier.fillMaxSize()) {
        // Navegación Lateral (Rail)
        NavigationDrawer(
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationDrawerItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            leadingContent = {
                                Icon(icons[index], contentDescription = null)
                            },
                            content = {
                                Text(text = title)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        ) {
            // Contenido Principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                when (selectedTab) {
                    0 -> EventsScreen()
                    1 -> TourismScreen()
                }
            }
        }
    }
}
