package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToAdmin: () -> Unit = {}
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Inicio", "Madurez", "Riego", "Historial")
    
    val icons = listOf(
        Icons.Default.GridView,
        Icons.Default.Grass,
        Icons.Default.Opacity,
        Icons.Default.Analytics
    )

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
                0 -> DashboardContent(viewModel, onNavigateToAdmin)
                1 -> MaturationContent()
                2 -> IrrigationScreen()
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Historial (En desarrollo)")
                }
            }
        }
    }
}

@Composable
fun DashboardContent(viewModel: MainViewModel, onNavigateToAdmin: () -> Unit) {
    val varieties = listOf(
        "Merlot" to 0.82f,
        "Viognier" to 0.71f,
        "Garnacha" to 0.68f
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Eco-Viñedo", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E3DE)
                )
                Text("Temporada 2026", color = Color.Gray, fontSize = 14.sp)
            }
            Row {
                IconButton(onClick = onNavigateToAdmin) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color(0xFFB4F391))
                }
                IconButton(onClick = { viewModel.reloadParcelas() }) {
                    Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = Color(0xFFB4F391))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFB4F391))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF384B2F))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFFB4F391))
                        Spacer(Modifier.width(8.dp))
                        Text("Índice madurez global", fontSize = 14.sp, color = Color.White)
                    }
                    Text("74%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB4F391))
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.74f },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color(0xFFB4F391),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Umbral óptimo: 85%  ·  18 días estimados", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            M3StatCard(title = "Parcelas activas", value = "9", icon = Icons.Default.Map, modifier = Modifier.weight(1f))
            M3StatCard(title = "Alertas", value = "2", icon = Icons.Default.Warning, modifier = Modifier.weight(1f), isAlert = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "MADURACIÓN POR VARIEDAD", 
            style = MaterialTheme.typography.labelLarge, 
            color = Color(0xFFB4F391),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(varieties) { item ->
                MaturityRow(item.first, item.second)
            }
        }
    }
}

@Composable
fun M3StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isAlert) Color(0xFF3D1916) else Color(0xFF1A1C18)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isAlert) Color(0xFFD32F2F) else Color(0xFF43493E))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isAlert) Color(0xFFF2B8B5) else Color.Gray)
                Spacer(Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = if (isAlert) Color(0xFFF2B8B5) else Color.Gray)
            }
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (isAlert) Color(0xFFF2B8B5) else Color.White)
        }
    }
}

@Composable
fun MaturationContent() {
    val tableData = listOf(
        Triple("Merlot", "82", "3.42"),
        Triple("Viognier", "71", "3.28"),
        Triple("Garnacha", "68", "3.35")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Maduración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE2E3DE))
        Text("Temporada 2026", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1A1C18),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43493E))
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF384B2F)).padding(12.dp)) {
                    Text("Variedad", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text("Brix", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text("pH", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text("Acidez", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text("Est.", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
                
                tableData.forEach { data ->
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(data.first, modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.White)
                        Text(data.second, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
                        Text(data.third, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.White)
                        Text("6.1", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.White)
                        AssistChip(
                            onClick = { },
                            label = { Text("94%", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(labelColor = Color(0xFFB4F391))
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF43493E)))
                }
            }
        }
    }
}

@Composable
fun MaturityRow(variety: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(variety, modifier = Modifier.width(90.dp), fontSize = 14.sp, color = Color.White)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(10.dp),
            color = if (progress > 0.8f) Color(0xFFB4F391) else Color(0xFFE2E3DE),
            trackColor = Color.Gray.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("${(progress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB4F391))
    }
}
