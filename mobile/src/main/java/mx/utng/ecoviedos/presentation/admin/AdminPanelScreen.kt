package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateToAddParcel: () -> Unit,
    onNavigateToSamples: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val adminOptions = listOf(
        AdminOption("Gestión Parcelas", Icons.Default.Map, onNavigateToAddParcel, "Registra o edita parcelas"),
        AdminOption("Registro Muestras", Icons.Default.Science, onNavigateToSamples, "Brix, pH y acidez"),
        AdminOption("Usuarios", Icons.Default.People, onNavigateToUsers, "Gestionar personal"),
        AdminOption("Configuración", Icons.Default.Settings, onNavigateToSettings, "Ajustes del sistema")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Salir", tint = Color(0xFFFFB4AB))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White
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
                "Bienvenido, Administrador",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Control central de Eco-Viñedo",
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
                items(adminOptions) { option ->
                    AdminCard(option)
                }
            }
        }
    }
}

data class AdminOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val description: String
)

@Composable
fun AdminCard(option: AdminOption) {
    ElevatedCard(
        onClick = option.onClick,
        modifier = Modifier.fillMaxWidth().height(160.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF384B2F)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                option.icon,
                contentDescription = null,
                tint = Color(0xFFB4F391),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    option.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    option.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
