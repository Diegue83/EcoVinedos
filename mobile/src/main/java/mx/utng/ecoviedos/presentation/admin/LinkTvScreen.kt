package mx.utng.ecoviedos.presentation.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.remote.LinkTvRequest
import mx.utng.ecoviedos.data.remote.RetrofitClient
import mx.utng.ecoviedos.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkTvScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEnologo: () -> Unit,
    mainViewModel: MainViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    
    val token by mainViewModel.sessionToken.collectAsState(initial = "")

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                code = result.contents
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular Smart TV", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1C18), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { 
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Escanea el QR de la TV")
                    options.setBeepEnabled(true)
                    options.setOrientationLocked(true) // Forzar orientación actual (Vertical)
                    scanLauncher.launch(options)
                },
                modifier = Modifier.size(100.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", modifier = Modifier.size(64.dp), tint = Color(0xFFB4F391))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Escanea el código QR o ingresa el código manual",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.uppercase() },
                label = { Text("CÓDIGO DE VINCULACIÓN") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ABC123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB4F391))
            )

            if (message != null) {
                Text(
                    text = message!!,
                    color = if (isError) Color.Red else Color(0xFFB4F391),
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    mainViewModel.viewModelScope.launch {
                        try {
                            val response = RetrofitClient.tvService.linkTV(
                                "Bearer $token",
                                LinkTvRequest(code)
                            )
                            if (response.isSuccessful) {
                                message = "¡Sincronización Exitosa! Redirigiendo..."
                                isError = false
                                code = ""
                                kotlinx.coroutines.delay(2000)
                                onNavigateToEnologo()
                            } else {
                                message = "Código inválido o expirado"
                                isError = true
                            }
                        } catch (e: Exception) {
                            message = "Error de conexión"
                            isError = true
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = code.length >= 4 && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4F391), contentColor = Color.Black)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Vincular Dispositivo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
