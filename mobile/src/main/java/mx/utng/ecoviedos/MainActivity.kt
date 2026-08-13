package mx.utng.ecoviedos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.utng.ecoviedos.presentation.auth.LoginScreen
import mx.utng.ecoviedos.presentation.auth.ForgotPasswordScreen
import mx.utng.ecoviedos.presentation.auth.VerifyCodeScreen
import mx.utng.ecoviedos.presentation.auth.ResetPasswordScreen
import mx.utng.ecoviedos.presentation.main.MainScreen
import mx.utng.ecoviedos.presentation.main.MainViewModel
import mx.utng.ecoviedos.presentation.main.HistorialViewModel
import mx.utng.ecoviedos.presentation.main.ParcelDetailsScreen
import mx.utng.ecoviedos.presentation.main.RegisterSampleScreen
import mx.utng.ecoviedos.presentation.main.NotificationScreen
import mx.utng.ecoviedos.presentation.admin.AdminPanelScreen
import mx.utng.ecoviedos.presentation.admin.AdminViewModel
import mx.utng.ecoviedos.presentation.admin.AddParcelScreen
import mx.utng.ecoviedos.presentation.admin.ParcelManagementScreen
import mx.utng.ecoviedos.presentation.admin.SampleRecordsScreen
import mx.utng.ecoviedos.presentation.admin.UserManagementScreen
import mx.utng.ecoviedos.presentation.admin.SettingsScreen
import mx.utng.ecoviedos.presentation.admin.DeviceConfigScreen
import mx.utng.ecoviedos.presentation.admin.DeviceConfigViewModel
import mx.utng.ecoviedos.presentation.admin.TourismManagementScreen
import mx.utng.ecoviedos.presentation.admin.AddEventScreen
import mx.utng.ecoviedos.presentation.admin.LinkTvScreen
import mx.utng.ecoviedos.presentation.enologo.EnologoMainScreen
import mx.utng.ecoviedos.presentation.enologo.CavaStateScreen
import mx.utng.ecoviedos.presentation.enologo.CavaManagementScreen
import mx.utng.ecoviedos.presentation.theme.EcoViedosTheme

class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualizar el intent para que NavHost lo vea
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            EcoViedosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Inicializar ViewModels para persistencia durante la sesión
                    val mainViewModel: MainViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()
                    val configViewModel: DeviceConfigViewModel = viewModel()
                    val historialViewModel: HistorialViewModel = viewModel()
                    
                    // Conectar ViewModels para el testeo local
                    adminViewModel.setMainViewModel(mainViewModel)

                    val token by mainViewModel.sessionToken.collectAsState(initial = "loading")
                    val userRol by mainViewModel.sessionRol.collectAsState(initial = "")

                    if (token != "loading") {
                        NavHost(
                            navController = navController,
                            startDestination = when {
                                token.isNullOrBlank() -> "login"
                                userRol == "enologo" -> "enologo_panel"
                                else -> "main"
                            }
                        ) {
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = { rol ->
                                        if (rol == "enologo") {
                                            navController.navigate("enologo_panel") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    },
                                    onForgotPassword = { navController.navigate("forgot_password") }
                                )
                            }
                            composable("enologo_panel") {
                                EnologoMainScreen(
                                    mainViewModel = mainViewModel,
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("enologo_panel") { inclusive = true }
                                        }
                                    },
                                    onNavigateToAddActivity = { navController.navigate("add_event") },
                                    onNavigateToEditActivity = { id -> navController.navigate("add_event?id=$id") },
                                    onNavigateToLinkSensor = { id, name -> 
                                        navController.navigate("device_config?targetId=$id&targetName=$name&type=CAVA")
                                    }
                                )
                            }
                            composable("cava_state") {
                                CavaStateScreen(onNavigateBack = { navController.popBackStack() })
                            }
                            composable("cava_management") {
                                CavaManagementScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToLinkSensor = TODO()
                                )
                            }
                            composable("forgot_password") {
                                ForgotPasswordScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onCodeSent = { email -> 
                                        navController.navigate("verify_code/$email")
                                    }
                                )
                            }
                            composable("verify_code/{email}") { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email") ?: ""
                                VerifyCodeScreen(
                                    email = email,
                                    onNavigateBack = { navController.popBackStack() },
                                    onCodeVerified = { code ->
                                        navController.navigate("reset_password/$email/$code")
                                    }
                                )
                            }
                            composable("reset_password/{email}/{code}") { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email") ?: ""
                                val code = backStackEntry.arguments?.getString("code") ?: ""
                                ResetPasswordScreen(
                                    email = email,
                                    code = code,
                                    onPasswordReset = {
                                        navController.navigate("login") {
                                            popUpTo("forgot_password") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("main") {
                                val navigateTo = intent.getStringExtra("navigate_to")
                                val initialTab = if (navigateTo == "riego") 2 else 0
                                // Limpiar el extra para que no se repita en recomposiciones
                                intent.removeExtra("navigate_to")
                                
                                MainScreen(
                                    viewModel = mainViewModel,
                                    historialViewModel = historialViewModel,
                                    initialTab = initialTab,
                                    onNavigateToAdmin = { navController.navigate("admin") },
                                    onNavigateToParcelDetails = { id -> navController.navigate("parcel_details/$id") },
                                    onNavigateToNotifications = { navController.navigate("notifications") },
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("admin") {
                                val userRol by mainViewModel.sessionRol.collectAsState(initial = "")
                                AdminPanelScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToParcelManagement = { navController.navigate("parcel_management") },
                                    onNavigateToTourismManagement = { navController.navigate("tourism_management") },
                                    onNavigateToEnologoMode = { navController.navigate("enologo_panel") },
                                    onNavigateToLinkTv = { navController.navigate("link_tv") },
                                    onNavigateToSamples = { }, // Ya no se usa desde aquí
                                    onNavigateToUsers = { navController.navigate("users") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToDeviceConfig = { navController.navigate("device_config") },
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    userRol = userRol ?: ""
                                )
                            }
                            composable("link_tv") {
                                LinkTvScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    mainViewModel = mainViewModel
                                )
                            }
                            composable("tourism_management") {
                                TourismManagementScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAdd = { navController.navigate("add_event") },
                                    onNavigateToEdit = { id -> navController.navigate("add_event?id=$id") }
                                )
                            }
                            composable(
                                route = "add_event?id={id}",
                                arguments = listOf(
                                    navArgument("id") {
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id")
                                AddEventScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    eventId = id
                                )
                            }
                            composable("parcel_details/{id}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id") ?: ""
                                val userRol by mainViewModel.sessionRol.collectAsState(initial = "")
                                ParcelDetailsScreen(
                                    parcelId = id,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToRegisterSample = { navController.navigate("register_sample/$id") },
                                    mainViewModel = mainViewModel,
                                    userRol = userRol ?: ""
                                )
                            }
                            composable("register_sample/{id}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id") ?: ""
                                RegisterSampleScreen(
                                    parcelId = id,
                                    onNavigateBack = {
                                        mainViewModel.cargarParcelas()
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("parcel_management") {
                                ParcelManagementScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAdd = { navController.navigate("add_parcel") },
                                    onNavigateToEdit = { id -> navController.navigate("add_parcel?id=$id") },
                                    viewModel = mainViewModel,
                                    adminViewModel = adminViewModel
                                )
                            }
                            composable(
                                route = "device_config?targetId={targetId}&targetName={targetName}&type={type}",
                                arguments = listOf(
                                    navArgument("targetId") { nullable = true; defaultValue = null },
                                    navArgument("targetName") { nullable = true; defaultValue = null },
                                    navArgument("type") { nullable = true; defaultValue = "PARCELA" }
                                )
                            ) { backStackEntry ->
                                val targetId = backStackEntry.arguments?.getString("targetId")
                                val targetName = backStackEntry.arguments?.getString("targetName")
                                val type = backStackEntry.arguments?.getString("type") ?: "PARCELA"
                                
                                DeviceConfigScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAddParcel = { navController.navigate("add_parcel") },
                                    mainViewModel = mainViewModel,
                                    configViewModel = configViewModel,
                                    preselectedId = targetId,
                                    preselectedName = targetName,
                                    linkType = type
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = mainViewModel
                                )
                            }
                            composable("users") {
                                UserManagementScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    adminViewModel = adminViewModel
                                )
                            }
                            composable(
                                route = "add_parcel?id={id}",
                                arguments = listOf(
                                    navArgument("id") {
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id")
                                AddParcelScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    adminViewModel = adminViewModel,
                                    parcelId = id,
                                    mainViewModel = mainViewModel
                                )
                            }
                            composable("samples") {
                                SampleRecordsScreen(onNavigateBack = { navController.popBackStack() })
                            }
                            composable("notifications") {
                                NotificationScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
