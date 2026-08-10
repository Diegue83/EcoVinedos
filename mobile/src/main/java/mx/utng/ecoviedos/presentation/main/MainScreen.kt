package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToParcelDetails: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val parcelas by viewModel.parcelas.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val userRol by viewModel.sessionRol.collectAsState(initial = "")
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    val items = listOf("Inicio", "Madurez", "Riego", "Historial")
    
    LaunchedEffect(selectedItem) {
        viewModel.cargarParcelas()
    }
    
    val icons = listOf(
        Icons.Default.GridView,
        Icons.Default.Grass,
        Icons.Default.Opacity,
        Icons.Default.History
    )

    Scaffold(
        topBar = {
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color(0xFFB4F391),
                    trackColor = Color.Transparent
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1C18),
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFB4F391),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFFB4F391),
                            indicatorColor = Color(0xFF384B2F)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedItem) {
                0 -> DashboardContent(viewModel, parcelas, onNavigateToAdmin, onLogout, userRol ?: "")
                1 -> MaturationContent(parcelas, onNavigateToParcelDetails, onRefresh = { viewModel.cargarParcelas() }, userRol = userRol ?: "")
                2 -> IrrigationScreen(parcelas, viewModel)
                3 -> HistoryScreen(parcelas)
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Configuración (En desarrollo)")
                }
            }
        }
    }
}
