package mx.utng.ecoviedos.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val WearColors = ColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,

    secondary = secondaryDark,
    onSecondary = onSecondaryDark,

    background = backgroundDark,
    onBackground = onBackgroundDark,

    onSurface = onSurfaceDark,

    error = errorDark,
    onError = onErrorDark
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WearColors,
        typography = AppTypography,
        content = content
    )
}