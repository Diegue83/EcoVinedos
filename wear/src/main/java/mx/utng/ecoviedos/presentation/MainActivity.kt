package mx.utng.ecoviedos.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.repository.BitacoraRepositoryImpl
import mx.utng.ecoviedos.domain.usecase.GuardarBitacoraUseCase
import mx.utng.ecoviedos.domain.usecase.ObtenerBitacorasUseCase
import mx.utng.ecoviedos.presentation.screens.*
import mx.utng.ecoviedos.presentation.theme.AppTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val repository = BitacoraRepositoryImpl()
        val viewModel = BitacoraViewModel(
            application = application,
            guardarBitacoraUseCase = GuardarBitacoraUseCase(repository),
            obtenerBitacorasUseCase = ObtenerBitacorasUseCase(repository)
        )

        setContent {
            AppTheme {
                val navController = rememberSwipeDismissableNavController()
                
                SwipeDismissableNavHost(
                    navController = navController, 
                    startDestination = "main_pager"
                ) {
                    composable("main_pager") {
                        MainPagerScreen(viewModel, onNavigateToSuccess = { 
                            navController.navigate("irrigation_success") 
                        })
                    }
                    composable("irrigation_success") {
                        val uiState by viewModel.uiState.collectAsState()
                        val selectedId by viewModel.selectedParcelId.collectAsState()
                        val selectedParcel = uiState.parcelas.find { it.id == selectedId }
                        
                        IrrigationSuccessScreen(
                            nombreParcela = "Parcela ${selectedParcel?.id ?: ""} - ${selectedParcel?.nombreParcela ?: ""}",
                            onDismiss = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainPagerScreen(viewModel: BitacoraViewModel, onNavigateToSuccess: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedId by viewModel.selectedParcelId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    val selectedParcel = uiState.parcelas.find { it.id == selectedId }
    val hasAlert = selectedParcel?.esHumedadCritica() == true
    
    val pagerState = rememberPagerState(pageCount = { 4 })

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(hasAlert) {
        if (hasAlert) {
            pagerState.animateScrollToPage(3)
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.parcelas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Conectando a Mosquitto...", textAlign = TextAlign.Center)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = true
                ) { page ->
                    when (page) {
                        0 -> ParcelDetailScreen(viewModel = viewModel, idParcela = selectedId)
                        1 -> MyParcelsScreen(
                            viewModel = viewModel,
                            onParcelClick = { id ->
                                viewModel.seleccionarParcela(id)
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                        )
                        2 -> BitacoraScreen(viewModel = viewModel, idParcela = selectedId)
                        3 -> {
                            if (hasAlert && selectedParcel != null) {
                                AlertScreen(
                                    idParcela = selectedParcel.id,
                                    nombreParcela = selectedParcel.nombreParcela,
                                    onActivateIrrigation = {
                                        viewModel.activarRiego(selectedParcel.id)
                                        onNavigateToSuccess()
                                    }
                                )
                            } else {
                                OptimalStateContent(selectedParcel?.nombreParcela, selectedParcel?.RIEGO_ACT)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)) {
                if (hasAlert && pagerState.currentPage != 3) {
                    Icon(
                        Icons.Default.Warning, 
                        contentDescription = null, 
                        tint = Color.Red,
                        modifier = Modifier.size(12.dp).align(Alignment.TopCenter).offset(y = (-15).dp)
                    )
                }
                
                HorizontalPageIndicator(
                    pageIndicatorState = object : PageIndicatorState {
                        override val pageOffset: Float get() = pagerState.currentPageOffsetFraction
                        override val selectedPage: Int get() = pagerState.currentPage
                        override val pageCount: Int get() = 4
                    }
                )
            }
        }
    }
}

@Composable
fun OptimalStateContent(nombrePar: String?, RIEGO_ACT: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle, 
                contentDescription = null, 
                tint = Color(0xFFB4F391),
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "PARCELA: ${nombrePar ?: ""}\nESTADO ÓPTIMO",
                textAlign = TextAlign.Center, 
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Riego: ${RIEGO_ACT ?: ""}",
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "Todo está bajo control", 
                textAlign = TextAlign.Center, 
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
