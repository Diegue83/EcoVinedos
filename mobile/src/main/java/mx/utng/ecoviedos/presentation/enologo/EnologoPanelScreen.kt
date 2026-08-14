package mx.utng.ecoviedos.presentation.enologo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoviedos.presentation.admin.AdminCard
import mx.utng.ecoviedos.presentation.admin.AdminOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnologoPanelScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventManagement: () -> Unit,
    onNavigateToCavaManagement: () -> Unit,
    onNavigateToCavaState: () -> Unit,
    onLogout: () -> Unit
) {
    val enologoOptions = listOf(
        AdminOption("Gestión de Eventos", Icons.Default.Event, onNavigateToEventManagement, "Registrar catas, tours y vendimias"),
        AdminOption("Control de Cavas", Icons.Default.Kitchen, onNavigateToCavaManagement, "Vincular sensores y contar botellas"),
        AdminOption("Estado en Vivo", Icons.Default.Analytics, onNavigateToCavaState, "Ver métricas de la cava en tiempo real"),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modo Enólogo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir", tint = Color(0xFFFFB4AB))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Bienvenido, Enólogo",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Control de calidad y experiencias",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(enologoOptions) { option ->
                    AdminCard(option)
                }
            }
        }
    }
}
