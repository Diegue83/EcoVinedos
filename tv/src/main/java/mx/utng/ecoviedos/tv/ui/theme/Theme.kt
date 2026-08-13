package mx.utng.ecoviedos.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorScheme = darkColorScheme(
    primary = VineyardLightGreen,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkBackground
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EcoViñedosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
