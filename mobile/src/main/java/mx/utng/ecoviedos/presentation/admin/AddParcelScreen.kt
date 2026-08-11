package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParcelScreen(
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = viewModel(),
    parcelId: String? = null,
    mainViewModel: MainViewModel = viewModel()
) {
    val parcelToEdit = remember(parcelId) {
        if (parcelId != null) {
            mainViewModel.parcelas.value.find { it.id == parcelId }
        } else null
    }

    var nombre by remember { mutableStateOf(parcelToEdit?.nombreParcela ?: "") }
    var variedad by remember { mutableStateOf(parcelToEdit?.variedad ?: "") }
    var area by remember { mutableStateOf(parcelToEdit?.areaM2?.toString() ?: "") }
    var umbralHumedad by remember { mutableStateOf(parcelToEdit?.umbralHumedad?.toInt()?.toString() ?: "30") }
    var umbralTemp by remember { mutableStateOf(parcelToEdit?.umbralTemp?.toInt()?.toString() ?: "25") }
    var umbralHumedadSuelo by remember { mutableStateOf(parcelToEdit?.umbralHumedadSuelo?.toInt()?.toString() ?: "40") }
    var humedadOptimaSuelo by remember { mutableStateOf(parcelToEdit?.humedadOptimaSuelo?.toInt()?.toString() ?: "70") }
    var consumoAguaM2 by remember { mutableStateOf(parcelToEdit?.consumoAguaM2?.toString() ?: "3.0") }
    var tipoRiego by remember { mutableStateOf(parcelToEdit?.tipoRiego ?: "MANUAL") }
    var activa by remember { mutableStateOf(parcelToEdit?.activa ?: true) }

    // Validaciones
    val areaNum = area.toIntOrNull() ?: 0
    val humNum = umbralHumedad.toFloatOrNull() ?: -1f
    val tempNum = umbralTemp.toFloatOrNull() ?: -100f
    val humSueloMinNum = umbralHumedadSuelo.toFloatOrNull() ?: -1f
    val humSueloOptNum = humedadOptimaSuelo.toFloatOrNull() ?: -1f
    val consumoNum = consumoAguaM2.toFloatOrNull() ?: 0f

    val isFormValid = nombre.isNotBlank() && 
                     variedad.isNotBlank() && 
                     areaNum > 0 && 
                     humNum in 0f..100f && 
                     tempNum in -20f..60f &&
                     humSueloMinNum in 0f..100f &&
                     humSueloOptNum in humSueloMinNum..100f &&
                     consumoNum > 0f

    val uiState by adminViewModel.uiState.collectAsState()
    val estaGuardando = uiState is AddParcelUiState.Loading

    // Navega de regreso solo cuando el backend confirma que se guardó
    LaunchedEffect(uiState) {
        if (uiState is AddParcelUiState.Success) {
            adminViewModel.resetState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (parcelId == null) "Nueva Parcela" else "Editar Parcela", fontWeight = FontWeight.Bold) },
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
            ExtendedFloatingActionButton(
                onClick = {
                    if (isFormValid && !estaGuardando) {
                        if (parcelId == null) {
                            adminViewModel.addParcel(
                                nombre,
                                variedad,
                                areaNum,
                                humNum,
                                tempNum,
                                humSueloMinNum,
                                humSueloOptNum,
                                consumoNum,
                                tipoRiego
                            )
                        } else {
                            adminViewModel.updateParcel(
                                parcelId,
                                nombre,
                                variedad,
                                areaNum,
                                humNum,
                                tempNum,
                                humSueloMinNum,
                                humSueloOptNum,
                                consumoNum,
                                activa,
                                tipoRiego
                            )
                        }
                    }
                },
                containerColor = if (isFormValid) Color(0xFFB4F391) else Color.Gray,
                contentColor = Color(0xFF1A1C18),
                icon = {
                    if (estaGuardando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
                text = { Text(if (estaGuardando) "Guardando..." else if (parcelId == null) "Guardar Parcela" else "Actualizar Parcela") }
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
            Text(
                "Información General",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB4F391)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Parcela") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombre.isBlank(),
                supportingText = { if (nombre.isBlank()) Text("El nombre es obligatorio") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB4F391),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFB4F391),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = variedad,
                onValueChange = { variedad = it },
                label = { Text("Variedad de Uva") },
                modifier = Modifier.fillMaxWidth(),
                isError = variedad.isBlank(),
                supportingText = { if (variedad.isBlank()) Text("La variedad es obligatoria") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB4F391),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFB4F391),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Área (m²)") },
                modifier = Modifier.fillMaxWidth(),
                isError = areaNum <= 0,
                supportingText = { if (areaNum <= 0) Text("Debe ser un número positivo") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB4F391),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFB4F391),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Text("Tipo de Válvula / Riego", color = Color(0xFFB4F391), style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = tipoRiego == "MANUAL",
                    onClick = { tipoRiego = "MANUAL" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB4F391))
                )
                Text("Manual", color = Color.White)
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = tipoRiego == "AUTO",
                    onClick = { tipoRiego = "AUTO" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB4F391))
                )
                Text("Auto", color = Color.White)
            }

            if (parcelId != null) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Parcela Activa", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = activa,
                        onCheckedChange = { activa = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFB4F391),
                            checkedTrackColor = Color(0xFF384B2F)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Configuración de Umbrales",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB4F391)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = umbralHumedad,
                    onValueChange = { umbralHumedad = it },
                    label = { Text("Humedad Mín (%)") },
                    modifier = Modifier.weight(1f),
                    isError = humNum !in 0f..100f,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = umbralTemp,
                    onValueChange = { umbralTemp = it },
                    label = { Text("Temp Máx (°C)") },
                    modifier = Modifier.weight(1f),
                    isError = tempNum !in -20f..60f,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = umbralHumedadSuelo,
                    onValueChange = { umbralHumedadSuelo = it },
                    label = { Text("H. Suelo Mín (%)") },
                    modifier = Modifier.weight(1f),
                    isError = humSueloMinNum !in 0f..100f,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = humedadOptimaSuelo,
                    onValueChange = { humedadOptimaSuelo = it },
                    label = { Text("H. Suelo Ópt (%)") },
                    modifier = Modifier.weight(1f),
                    isError = humSueloOptNum < humSueloMinNum,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB4F391),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFB4F391),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            OutlinedTextField(
                value = consumoAguaM2,
                onValueChange = { consumoAguaM2 = it },
                label = { Text("Consumo Agua (L/h por m²)") },
                modifier = Modifier.fillMaxWidth(),
                isError = consumoNum <= 0f,
                supportingText = { Text("Valor recomendado: 3.0 L/h") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB4F391),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFB4F391),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Mensaje de error si la creación falla en el backend
            if (uiState is AddParcelUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2F2F).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (uiState as AddParcelUiState.Error).mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFFB4AB),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF384B2F).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB4F391))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Estos umbrales se usarán para generar alertas automáticas en el panel de control y en el reloj.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
