package mx.utng.ecoviedos.tv.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import mx.utng.ecoviedos.tv.presentation.events.EventsScreen
import mx.utng.ecoviedos.tv.presentation.tourism.TourismScreen

enum class TvScreen {
    PAIRING,
    DASHBOARD,
    CAVA_DETAIL,
    EXPERIENCES,
    EVENTS,
    TOURISM
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainTvScreen(
    viewModel: TvViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(TvScreen.DASHBOARD) }
    var selectedDrawerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState) {
        if (uiState is TvUiState.NotLinked) {
            currentScreen = TvScreen.PAIRING
        } else if (uiState is TvUiState.Linked) {
            currentScreen = TvScreen.DASHBOARD
        }
    }

    if (currentScreen != TvScreen.PAIRING && currentScreen != TvScreen.DASHBOARD) {
        BackHandler {
            currentScreen = TvScreen.DASHBOARD
        }
    }

    when (val state = uiState) {
        is TvUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is TvUiState.NotLinked -> {
            PairingScreen(pairingCode = state.pairingCode)
        }
        is TvUiState.Linked, is TvUiState.Error -> {
            NavigationDrawer(
                drawerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        NavigationDrawerItem(
                            selected = selectedDrawerIndex == 0,
                            onClick = { 
                                selectedDrawerIndex = 0
                                currentScreen = TvScreen.DASHBOARD 
                            },
                            leadingContent = { Icon(Icons.Default.Home, contentDescription = null) }
                        ) {
                            Text("Inicio")
                        }
                        Spacer(Modifier.height(12.dp))
                        NavigationDrawerItem(
                            selected = selectedDrawerIndex == 1,
                            onClick = { 
                                selectedDrawerIndex = 1
                                currentScreen = TvScreen.EVENTS 
                            },
                            leadingContent = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                        ) {
                            Text("Eventos")
                        }
                        Spacer(Modifier.height(12.dp))
                        NavigationDrawerItem(
                            selected = selectedDrawerIndex == 2,
                            onClick = { 
                                selectedDrawerIndex = 2
                                currentScreen = TvScreen.TOURISM 
                            },
                            leadingContent = { Icon(Icons.Default.Explore, contentDescription = null) }
                        ) {
                            Text("Turismo")
                        }
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        TvScreen.DASHBOARD -> TvDashboardScreen(
                            onNavigateToCavaDetail = { currentScreen = TvScreen.CAVA_DETAIL },
                            onNavigateToPromotionsDetail = { currentScreen = TvScreen.EXPERIENCES }
                        )
                        TvScreen.CAVA_DETAIL -> CavaDetailScreen(
                            onNavigateBack = { currentScreen = TvScreen.DASHBOARD }
                        )
                        TvScreen.EXPERIENCES -> ExperiencesScreen()
                        TvScreen.EVENTS -> EventsScreen()
                        TvScreen.TOURISM -> TourismScreen()
                        else -> TvDashboardScreen({}, {})
                    }
                }
            }
        }
    }
}
