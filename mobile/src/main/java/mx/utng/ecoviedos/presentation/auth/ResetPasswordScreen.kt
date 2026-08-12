package mx.utng.ecoviedos.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    code: String,
    onPasswordReset: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onPasswordReset()
            authViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* Could go back but usually not recommended here */ }) {
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
                text = "Nueva contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Debe tener al menos 6 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            // Password Strength Indicator Placeholder
            if (newPassword.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val strength = when {
                        newPassword.length < 6 -> 1
                        newPassword.length < 10 -> 2
                        else -> 3
                    }
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    if (i < strength) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f),
                                    MaterialTheme.shapes.small
                                )
                        )
                    }
                }
                Text("Fortaleza: ${if (newPassword.length > 8) "Fuerte" else "Media"}", 
                    style = MaterialTheme.typography.bodySmall, 
                    modifier = Modifier.align(Alignment.End))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }
            
            if (newPassword != confirmPassword && confirmPassword.isNotEmpty()) {
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { authViewModel.restablecerContraseña(email, code, newPassword) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState !is AuthUiState.Loading && newPassword.length >= 6 && newPassword == confirmPassword,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Guardar nueva contraseña", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
