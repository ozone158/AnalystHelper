package org.example.theme

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// BMO Brand Colors
object BMOColors {
    // Primary BMO Blue - #003478 (BMO's signature blue)
    val BMOPrimaryBlue = Color(0xFF003478)
    val BMOPrimaryBlueLight = Color(0xFF004A9C)
    val BMOPrimaryBlueDark = Color(0xFF002350)
    
    // Secondary colors
    val BMOSecondaryBlue = Color(0xFF0066CC)
    val BMOAccentBlue = Color(0xFF0080FF)
    
    // Neutral colors
    val BMOGray = Color(0xFF6B7280)
    val BMOGrayLight = Color(0xFFF3F4F6)
    val BMOGrayDark = Color(0xFF374151)
    
    // Status colors
    val BMOSuccess = Color(0xFF10B981) // Green for approved
    val BMOWarning = Color(0xFFF59E0B) // Amber for in review
    val BMOError = Color(0xFFEF4444) // Red for declined
    val BMOInfo = Color(0xFF3B82F6) // Blue for pending
    
    // Background colors
    val BMOBackground = Color(0xFFFFFFFF)
    val BMOSurface = Color(0xFFF9FAFB)
    val BMOSurfaceVariant = Color(0xFFE5E7EB)
}

private val BMOLightColors = lightColors(
    primary = BMOColors.BMOPrimaryBlue,
    primaryVariant = BMOColors.BMOPrimaryBlueDark,
    secondary = BMOColors.BMOSecondaryBlue,
    secondaryVariant = BMOColors.BMOAccentBlue,
    background = BMOColors.BMOBackground,
    surface = BMOColors.BMOSurface,
    error = BMOColors.BMOError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BMOColors.BMOGrayDark,
    onSurface = BMOColors.BMOGrayDark,
    onError = Color.White
)

@Composable
fun BMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = BMOLightColors,
        typography = BMOTypography,
        shapes = BMOShapes,
        content = content
    )
}

// BMO Typography - Professional and clean
// Using default Typography as base and customizing it
private val BMOTypography = Typography(
    defaultFontFamily = androidx.compose.ui.text.font.FontFamily.Default
).let { default ->
    default.copy(
        h1 = default.h1.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = (-0.5).sp
        ),
        h2 = default.h2.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = 0.sp
        ),
        h3 = default.h3.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = 0.sp
        ),
        h4 = default.h4.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            letterSpacing = 0.25.sp
        ),
        h5 = default.h5.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        h6 = default.h6.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            letterSpacing = 0.15.sp
        ),
        subtitle1 = default.subtitle1.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            letterSpacing = 0.15.sp
        ),
        subtitle2 = default.subtitle2.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            letterSpacing = 0.1.sp
        ),
        body1 = default.body1.copy(
            letterSpacing = 0.5.sp
        ),
        body2 = default.body2.copy(
            letterSpacing = 0.25.sp
        ),
        button = default.button.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            letterSpacing = 1.25.sp
        ),
        caption = default.caption.copy(
            letterSpacing = 0.4.sp
        ),
        overline = default.overline.copy(
            letterSpacing = 1.5.sp
        )
    )
}

// BMO Shapes - Rounded corners for modern look
private val BMOShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
)
