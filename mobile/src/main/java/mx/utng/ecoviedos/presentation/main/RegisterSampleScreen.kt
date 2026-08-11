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
    var indiceMaduracion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Validaciones
    val brixNum = brix.toDoubleOrNull() ?: -1.0
    val phNum = ph.toDoubleOrNull() ?: -1.0
    val acidezNum = acidez.toDoubleOrNull() ?: -1.0
    val phSueloNum = phSuelo.toDoubleOrNull() ?: -1.0
    val maturityNum = indiceMaduracion.toDoubleOrNull() ?: -1.0

    val isFormValid = brixNum in 0.0..100.0 && 
                     phNum in 0.0..14.0 && 
                     acidezNum in 0.0..50.0 && 
                     phSueloNum in 0.0..14.0 &&
                     (indiceMaduracion.isBlank() || maturityNum in 0.0..100.0)

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
                label = { Text("Grados Brix (0-100)") },
                modifier = Modifier.fillMaxWidth(),
                isError = brix.isNotBlank() && brixNum !in 0.0..100.0,
                supportingText = { if (brix.isNotBlank() && brixNum !in 0.0..100.0) Text("Debe estar entre 0 y 100") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = ph,
                onValueChange = { ph = it },
                label = { Text("pH del Fruto (0-14)") },
                modifier = Modifier.fillMaxWidth(),
                isError = ph.isNotBlank() && phNum !in 0.0..14.0,
                supportingText = { if (ph.isNotBlank() && phNum !in 0.0..14.0) Text("Debe estar entre 0 y 14") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = acidez,
                onValueChange = { acidez = it },
                label = { Text("Acidez (g/L)") },
                modifier = Modifier.fillMaxWidth(),
                isError = acidez.isNotBlank() && acidezNum !in 0.0..50.0,
                supportingText = { if (acidez.isNotBlank() && acidezNum !in 0.0..50.0) Text("Valor inválido") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = phSuelo,
                onValueChange = { phSuelo = it },
                label = { Text("pH del Suelo (0-14)") },
                modifier = Modifier.fillMaxWidth(),
                isError = phSuelo.isNotBlank() && phSueloNum !in 0.0..14.0,
                supportingText = { if (phSuelo.isNotBlank() && phSueloNum !in 0.0..14.0) Text("Debe estar entre 0 y 14") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = indiceMaduracion,
                onValueChange = { indiceMaduracion = it },
                label = { Text("Índice de Maduración (0-100%)") },
                modifier = Modifier.fillMaxWidth(),
                isError = indiceMaduracion.isNotBlank() && maturityNum !in 0.0..100.0,
                supportingText = { if (indiceMaduracion.isNotBlank() && maturityNum !in 0.0..100.0) Text("Debe estar entre 0 y 100") },
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
                    if (isFormValid) {
                        muestraViewModel.registrarMuestra(
                            parcelId,
                            brixNum,
                            phNum,
                            acidezNum,
                            phSueloNum,
                            if (indiceMaduracion.isBlank()) null else maturityNum,
                            observaciones
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is MuestraUiState.Loading && isFormValid,
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
