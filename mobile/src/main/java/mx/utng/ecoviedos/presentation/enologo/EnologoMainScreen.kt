package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.presentation.admin.TourismManagementScreen
import mx.utng.ecoviedos.presentation.main.MainViewModel

@Composable
fun EnologoMainScreen(
    mainViewModel: MainViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onNavigateToAddActivity: () -> Unit = {},
    onNavigateToEditActivity: (String) -> Unit = {},
    onNavigateToLinkSensor: (String, String) -> Unit = { _, _ -> }
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf("Dashboard", "Actividades", "Cavas", "Estado")
    val icons = listOf(Icons.Default.Dashboard, Icons.Default.Event, Icons.Default.Kitchen, Icons.Default.Analytics)

    Scaffold(
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
                0 -> EnologoDashboardScreen(onLogout = onLogout)
                1 -> TourismManagementScreen(
                    onNavigateBack = {}, 
                    onNavigateToAdd = onNavigateToAddActivity,
                    onNavigateToEdit = onNavigateToEditActivity,
                    showBackButton = false
                )
                2 -> CavaManagementScreen(
                    onNavigateBack = {},
                    onNavigateToLinkSensor = onNavigateToLinkSensor,
                    mainViewModel = mainViewModel
                )
                3 -> CavaStateScreen(onNavigateBack = {})
            }
        }
    }
}
