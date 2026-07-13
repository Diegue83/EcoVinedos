package mx.utng.ecoviedos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.ecoviedos.presentation.auth.LoginScreen
import mx.utng.ecoviedos.presentation.main.MainScreen
import mx.utng.ecoviedos.presentation.main.MainViewModel
import mx.utng.ecoviedos.presentation.admin.AdminPanelScreen
import mx.utng.ecoviedos.presentation.admin.AdminViewModel
import mx.utng.ecoviedos.presentation.admin.AddParcelScreen
import mx.utng.ecoviedos.presentation.admin.SampleRecordsScreen
import mx.utng.ecoviedos.presentation.admin.UserManagementScreen
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
                    
                    // Conectar ViewModels para el testeo local
                    adminViewModel.setMainViewModel(mainViewModel)

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { 
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
                                onNavigateToAdmin = { navController.navigate("admin") }
                            )
                        }
                        composable("admin") {
                            AdminPanelScreen(
                                onNavigateToAddParcel = { navController.navigate("add_parcel") },
                                onNavigateToSamples = { navController.navigate("samples") },
                                onNavigateToUsers = { navController.navigate("users") },
                                onLogout = { 
                                    navController.navigate("login") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("users") {
                            UserManagementScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("add_parcel") {
                            AddParcelScreen(
                                onNavigateBack = { navController.popBackStack() },
                                adminViewModel = adminViewModel
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
