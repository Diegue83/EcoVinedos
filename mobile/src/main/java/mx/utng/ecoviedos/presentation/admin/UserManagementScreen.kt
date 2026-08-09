package mx.utng.ecoviedos.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import mx.utng.ecoviedos.data.remote.UsuarioResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val uiState by adminViewModel.userUiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UsuarioResponse?>(null) }
    var userToDelete by remember { mutableStateOf<UsuarioResponse?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.loadUsers()
    }

    if (showAddDialog || userToEdit != null) {
        UserFormDialog(
            user = userToEdit,
            onDismiss = { 
                showAddDialog = false
                userToEdit = null
            },
            onSave = { nombre, correo, pass, rol, tel ->
                if (userToEdit == null) {
                    adminViewModel.createUser(nombre, correo, pass ?: "", rol, tel)
                } else {
                    adminViewModel.updateUser(userToEdit!!._id, nombre, correo, rol, tel)
                }
                showAddDialog = false
                userToEdit = null
            }
        )
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Seguro que deseas eliminar a ${userToDelete?.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        adminViewModel.deleteUser(userToDelete!!._id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
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
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFB4F391),
                contentColor = Color(0xFF1A1C18)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Usuario")
            }
        },
        containerColor = Color(0xFF1A1C18)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UserManagementUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB4F391))
                }
                is UserManagementUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.users) { user ->
                            UserCard(
                                user = user,
                                onEdit = { userToEdit = user },
                                onDelete = { userToDelete = user }
                            )
                        }
                    }
                }
                is UserManagementUiState.Error -> {
                    Text(
                        text = state.mensaje,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserCard(user: UsuarioResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1D2024)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF43493E)))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = Color(0xFF384B2F)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFB4F391))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nombre, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user.rol.uppercase(), fontSize = 11.sp, color = Color(0xFFB4F391), fontWeight = FontWeight.Bold)
                Text(user.correo, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFB4F391))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFFB4AB))
            }
        }
    }
}

@Composable
fun UserFormDialog(
    user: UsuarioResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var correo by remember { mutableStateOf(user?.correo ?: "") }
    var contrasena by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(user?.rol ?: "trabajador") }
    var telefono by remember { mutableStateOf(user?.telefono ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })
                if (user == null) {
                    OutlinedTextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") })
                }
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
                
                Text("Rol:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = rol == "administrador", onClick = { rol = "administrador" })
                    Text("Admin", color = Color.White)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = rol == "trabajador", onClick = { rol = "trabajador" })
                    Text("Trabajador", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(nombre, correo, contrasena.takeIf { it.isNotBlank() }, rol, telefono) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
