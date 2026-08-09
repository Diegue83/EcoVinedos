package mx.utng.ecoviedos

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import mx.utng.ecoviedos.presentation.auth.LoginScreen
import mx.utng.ecoviedos.presentation.main.MainScreen
import mx.utng.ecoviedos.presentation.main.MainViewModel
import mx.utng.ecoviedos.presentation.main.ParcelDetailsScreen
import mx.utng.ecoviedos.presentation.main.RegisterSampleScreen
import mx.utng.ecoviedos.presentation.admin.AdminPanelScreen
import mx.utng.ecoviedos.presentation.admin.AdminViewModel
import mx.utng.ecoviedos.presentation.admin.AddParcelScreen
import mx.utng.ecoviedos.presentation.admin.ParcelManagementScreen
import mx.utng.ecoviedos.presentation.admin.SampleRecordsScreen
import mx.utng.ecoviedos.presentation.admin.UserManagementScreen
import mx.utng.ecoviedos.presentation.admin.SettingsScreen
import mx.utng.ecoviedos.presentation.admin.DeviceConfigScreen
import mx.utng.ecoviedos.presentation.admin.DeviceConfigViewModel
import mx.utng.ecoviedos.presentation.theme.EcoViedosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    
                    // Conectar ViewModels para el testeo local
                    adminViewModel.setMainViewModel(mainViewModel)

                    val token by mainViewModel.sessionToken.collectAsState(initial = "loading")

                    if (token != "loading") {
                        NavHost(
                            navController = navController,
                            startDestination = if (token.isNullOrBlank()) "login" else "main"
                        ) {
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = { rol ->
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onForgotPassword = { /* Navigate to recovery */ }
                                )
                            }
                            composable("main") {
                                MainScreen(
                                    viewModel = mainViewModel,
                                    onNavigateToAdmin = { navController.navigate("admin") },
                                    onNavigateToParcelDetails = { id -> navController.navigate("parcel_details/$id") },
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("admin") {
                                AdminPanelScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToParcelManagement = { navController.navigate("parcel_management") },
                                    onNavigateToSamples = { }, // Ya no se usa desde aquí
                                    onNavigateToUsers = { navController.navigate("users") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToDeviceConfig = { navController.navigate("device_config") },
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("parcel_details/{id}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id") ?: ""
                                ParcelDetailsScreen(
                                    parcelId = id,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToRegisterSample = { navController.navigate("register_sample/$id") },
                                    mainViewModel = mainViewModel
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
                            composable("device_config") {
                                DeviceConfigScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    mainViewModel = mainViewModel,
                                    configViewModel = configViewModel
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
                                    androidx.navigation.navArgument("id") {
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
                        }
                    }
                }
            }
        }
    }
}
