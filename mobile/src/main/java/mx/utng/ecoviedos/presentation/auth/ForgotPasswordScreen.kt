package mx.utng.ecoviedos.presentation.auth

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onCodeSent: (email: String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.CodeSent) {
            onCodeSent(email)
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
                    Text("📱", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recuperar contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ingresa tu correo y te enviaremos un código para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico registrado") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = { Text("usuario@vinedo.mx") }
            )

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.solicitarCodigo(email.trim()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState !is AuthUiState.Loading && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Enviar enlace de recuperación", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onNavigateBack) {
                Text("Cancelar - volver al login", color = Color.Gray)
            }
        }
    }
}
