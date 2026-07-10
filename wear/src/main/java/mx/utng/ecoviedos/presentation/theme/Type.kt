package mx.utng.ecoviedos.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Typography

val AppTypography = Typography(

    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),

    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),

    bodyLarge = TextStyle(
        fontSize = 15.sp
    ),

    bodyMedium = TextStyle(
        fontSize = 13.sp
    ),

    bodySmall = TextStyle(
        fontSize = 11.sp
    ),

    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),

    labelMedium = TextStyle(
        fontSize = 11.sp
    ),

    labelSmall = TextStyle(
        fontSize = 10.sp
    )
)