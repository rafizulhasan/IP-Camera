package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PitchBlackColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = PitchBlack,
    primaryContainer = CyberCyanContainer,
    onPrimaryContainer = CyberCyan,
    secondary = LanEmerald,
    onSecondary = PitchBlack,
    secondaryContainer = LanEmeraldContainer,
    onSecondaryContainer = LanEmerald,
    tertiary = WarningAmber,
    onTertiary = PitchBlack,
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = WarningAmber,
    error = AlertCrimson,
    onError = Color.White,
    errorContainer = AlertCrimsonContainer,
    onErrorContainer = AlertCrimson,
    background = PitchBlack,
    onBackground = TextPureWhite,
    surface = PitchBlack,
    onSurface = TextPureWhite,
    surfaceVariant = CarbonCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = PitchBlack,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = CarbonCard,
    surfaceContainerHigh = ElevatedCarbon,
    surfaceContainerHighest = GlassBorder,
    outline = GlassBorder,
    outlineVariant = SubtleGridLine
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PitchBlack.toArgb()
                window.navigationBarColor = PitchBlack.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = PitchBlackColorScheme,
        typography = Typography,
        content = content
    )
}
