package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EduSoalLightColorScheme = lightColorScheme(
    primary = SlateInk,
    onPrimary = Color.White,
    primaryContainer = SurfaceContainerLow,
    onPrimaryContainer = OnSurfaceDeep,
    secondary = AcademicBlue,
    onSecondary = Color.White,
    secondaryContainer = AcademicBlueContainer,
    onSecondaryContainer = OnAcademicBlueContainer,
    tertiary = EmeraldKey,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldKeySurface,
    onTertiaryContainer = EmeraldKeyText,
    background = SurfaceCanvas,
    onBackground = OnSurfaceDeep,
    surface = SurfaceCard,
    onSurface = OnSurfaceDeep,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariantText,
    surfaceContainerLowest = SurfaceCard,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = BorderInput,
    outlineVariant = BorderHairline,
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorSurface,
    onErrorContainer = OnErrorSurface
)

private val EduSoalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0),
    onPrimary = SlateInk,
    primaryContainer = DarkSurfaceContainer,
    onPrimaryContainer = DarkTextPrimary,
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF00174B),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = EmeraldVibrant,
    onTertiary = Color(0xFF002113),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DarkSurfaceCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderHairline,
    outlineVariant = DarkBorderHairline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// Tight structural 4.dp micro-geometry per EduSoal AI design system
val EduSoalShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun EduSoalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EduSoalDarkColorScheme else EduSoalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduSoalTypography,
        shapes = EduSoalShapes,
        content = content
    )
}
