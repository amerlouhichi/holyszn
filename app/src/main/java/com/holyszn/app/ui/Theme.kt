package com.holyszn.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val Void = Color(0xFF07070A)
val Panel = Color(0xFF121216)
val Red = Color(0xFFFF2233)
val DeepRed = Color(0xFF8E0010)
val Chrome1 = Color(0xFFF2F4F8)
val Chrome2 = Color(0xFF8A8F9A)
val ChromeDark = Color(0xFF3A3D45)

val ChromeBrush = Brush.horizontalGradient(listOf(Chrome1, Chrome2, Chrome1, Red))

private val scheme = darkColorScheme(
    primary = Red, onPrimary = Color.White,
    secondary = Chrome2, background = Void, onBackground = Chrome1,
    surface = Panel, onSurface = Chrome1, surfaceVariant = Panel,
    onSurfaceVariant = Chrome2, outline = ChromeDark
)

@Composable
fun HolyTheme(content: @Composable () -> Unit) {
    val base = Typography()
    val mono = FontFamily.Monospace
    val typo = base.copy(
        titleLarge = base.titleLarge.copy(fontFamily = mono),
        titleMedium = base.titleMedium.copy(fontFamily = mono),
        labelLarge = base.labelLarge.copy(fontFamily = mono),
        labelMedium = base.labelMedium.copy(fontFamily = mono),
        labelSmall = base.labelSmall.copy(fontFamily = mono)
    )
    MaterialTheme(colorScheme = scheme, typography = typo, content = content)
}
