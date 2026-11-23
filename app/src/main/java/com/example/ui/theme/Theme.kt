package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class LedgerExtendedColors(
    val surfaceContainerLow: Color,
    val cardBackground: Color,
    val borderTint: Color,
    val emerald: Color,
    val emeraldDeep: Color,
    val emeraldContainer: Color,
    val coral: Color,
    val coralContainer: Color,
    val amber: Color,
    val amberContainer: Color,
    val textSecondary: Color,
    val heroGradient: Brush
)

val LocalLedgerColors = staticCompositionLocalOf {
    LedgerExtendedColors(
        surfaceContainerLow = SurfaceContainerLow,
        cardBackground = SurfaceCard,
        borderTint = SurfaceDimBorder,
        emerald = EmeraldAccent,
        emeraldDeep = EmeraldDeep,
        emeraldContainer = EmeraldMintContainer,
        coral = CoralWarning,
        coralContainer = CoralContainer,
        amber = AmberWarning,
        amberContainer = AmberContainer,
        textSecondary = TextSecondary,
        heroGradient = Brush.linearGradient(listOf(SapphirePrimary, SapphireDeep))
    )
}

private val ClarityLightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.White,
    primaryContainer = SapphireContainer,
    onPrimaryContainer = SapphirePrimary,
    secondary = SapphireDeep,
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainerLow,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldDeep,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldMintContainer,
    onTertiaryContainer = EmeraldDeep,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceDimBorder,
    error = CoralWarning,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = CoralWarning
)

private val ClarityDarkColorScheme = darkColorScheme(
    primary = DarkPrimarySapphire,
    onPrimary = SapphireDeep,
    primaryContainer = SapphirePrimary,
    onPrimaryContainer = Color.White,
    secondary = EmeraldAccent,
    onSecondary = SapphireDeep,
    secondaryContainer = DarkSurfaceContainerLow,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = EmeraldAccent,
    onTertiary = SapphireDeep,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = EmeraldMintBright,
    background = DarkSurfaceBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainerLow,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderTint,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun SubJoggerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ClarityDarkColorScheme else ClarityLightColorScheme
    val extended = if (darkTheme) {
        LedgerExtendedColors(
            surfaceContainerLow = DarkSurfaceContainerLow,
            cardBackground = DarkSurfaceCard,
            borderTint = DarkBorderTint,
            emerald = EmeraldAccent,
            emeraldDeep = EmeraldMintBright,
            emeraldContainer = Color(0xFF064E3B),
            coral = Color(0xFFF87171),
            coralContainer = Color(0xFF450A0A),
            amber = Color(0xFFFBBF24),
            amberContainer = Color(0xFF451A03),
            textSecondary = DarkTextSecondary,
            heroGradient = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF090F1E)))
        )
    } else {
        LedgerExtendedColors(
            surfaceContainerLow = SurfaceContainerLow,
            cardBackground = SurfaceCard,
            borderTint = SurfaceDimBorder,
            emerald = EmeraldAccent,
            emeraldDeep = EmeraldDeep,
            emeraldContainer = EmeraldMintContainer,
            coral = CoralWarning,
            coralContainer = CoralContainer,
            amber = AmberWarning,
            amberContainer = AmberContainer,
            textSecondary = TextSecondary,
            heroGradient = Brush.linearGradient(listOf(SapphirePrimary, SapphireDeep))
        )
    }

    CompositionLocalProvider(LocalLedgerColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SubJoggerTypography,
            content = content
        )
    }
}
