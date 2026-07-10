package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleRecordsScreen(onNavigateBack: () -> Unit) {
    // Datos mock para visualización
    val samples = listOf(
        SampleData("Merlot - Parcela 4", "12 Oct 2026", "23.5", "3.42", "6.2"),
        SampleData("Viognier - Parcela 7", "11 Oct 2026", "21.2", "3.28", "5.8"),
        SampleData("Garnacha - Parcela 9", "10 Oct 2026", "22.8", "3.35", "6.0"),
        SampleData("Merlot - Parcela 4", "05 Oct 2026", "22.1", "3.38", "6.1")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muestras de Laboratorio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C18),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Abrir formulario de nueva muestra */ },
                containerColor = Color(0xFFB4F391),
                contentColor = Color(0xFF1A1C18)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Muestra")
            }
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(samples) { sample ->
                    SampleItem(sample)
                }
            }
        }
    }
}

data class SampleData(
    val parcela: String,
    val fecha: String,
    val brix: String,
    val ph: String,
    val acidez: String
)

@Composable
fun SampleItem(sample: SampleData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43493E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFFB4F391), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sample.parcela, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(sample.fecha, fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SampleStat("Brix", sample.brix)
                SampleStat("pH", sample.ph)
                SampleStat("Acidez", sample.acidez)
            }
        }
    }
}

@Composable
fun SampleStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 18.sp, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
    }
}
