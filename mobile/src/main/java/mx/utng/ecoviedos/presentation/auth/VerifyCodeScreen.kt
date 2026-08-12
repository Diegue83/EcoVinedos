package mx.utng.ecoviedos.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onCodeVerified: (code: String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.CodeVerified) {
            onCodeVerified(code)
            authViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF2E7D32).copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Correo enviado",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enviamos el enlace a:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3897F0)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Código de verificación (6 dígitos)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom 6-digit input
            BasicTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { index ->
                            val char = when {
                                index >= code.length -> ""
                                else -> code[index].toString()
                            }
                            val isFocused = code.length == index
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(
                                        1.dp,
                                        if (isFocused) Color(0xFF3897F0) else Color.Gray.copy(alpha = 0.5f),
                                        MaterialTheme.shapes.small
                                    )
                                    .background(Color.Gray.copy(alpha = 0.1f), MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏳ El código expira en: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("11:42", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Yellow)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.verificarCodigo(email, code) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState !is AuthUiState.Loading && code.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Verificar código", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = { authViewModel.solicitarCodigo(email) }) {
                Text("No recibí el correo - reenviar", color = Color(0xFF3897F0))
            }
        }
    }
}
