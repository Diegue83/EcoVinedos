package mx.utng.ecoviedos.tv.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*

enum class TvScreen {
    PAIRING,
    DASHBOARD,
    CAVA_DETAIL,
    ACTIVITIES
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainTvScreen(
    viewModel: TvViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(TvScreen.DASHBOARD) }

    LaunchedEffect(uiState) {
        if (uiState is TvUiState.NotLinked) {
            currentScreen = TvScreen.PAIRING
        } else if (uiState is TvUiState.Linked) {
            currentScreen = TvScreen.DASHBOARD
        }
    }

    // Manejo del botón Atrás del control remoto
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
        is TvUiState.Linked -> {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    TvScreen.DASHBOARD -> TvDashboardScreen(
                        cavas = state.cavas,
                        onNavigateToCavaDetail = { currentScreen = TvScreen.CAVA_DETAIL },
                        onNavigateToActivities = { currentScreen = TvScreen.ACTIVITIES }
                    )
                    TvScreen.CAVA_DETAIL -> CavaDetailScreen(
                        cavas = state.cavas,
                        onNavigateBack = { currentScreen = TvScreen.DASHBOARD }
                    )
                    TvScreen.ACTIVITIES -> ActivitiesScreen() 
                }
            }
        }
        is TvUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
        else -> {
            // Handle other states if any
        }
    }
}
