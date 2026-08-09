package mx.utng.ecoviedos.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSampleScreen(
    parcelId: String,
    onNavigateBack: () -> Unit,
    muestraViewModel: MuestraViewModel = viewModel()
) {
    var brix by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var acidez by remember { mutableStateOf("") }
    var phSuelo by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val registroExitoso by muestraViewModel.registroExitoso.collectAsState()
    val uiState by muestraViewModel.uiState.collectAsState()

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            muestraViewModel.resetRegistroState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Muestra", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = brix,
                onValueChange = { brix = it },
                label = { Text("Grados Brix (°)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = ph,
                onValueChange = { ph = it },
                label = { Text("pH del Fruto") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = acidez,
                onValueChange = { acidez = it },
                label = { Text("Acidez (g/L)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = phSuelo,
                onValueChange = { phSuelo = it },
                label = { Text("pH del Suelo") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones Adicionales") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            if (uiState is MuestraUiState.Error) {
                Text((uiState as MuestraUiState.Error).mensaje, color = Color.Red)
            }

            Button(
                onClick = {
                    val b = brix.toDoubleOrNull() ?: 0.0
                    val p = ph.toDoubleOrNull() ?: 0.0
                    val a = acidez.toDoubleOrNull() ?: 0.0
                    val ps = phSuelo.toDoubleOrNull() ?: 0.0
                    muestraViewModel.registrarMuestra(parcelId, b, p, a, ps, observaciones)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is MuestraUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
            ) {
                if (uiState is MuestraUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Muestra")
                }
            }
        }
    }
}
