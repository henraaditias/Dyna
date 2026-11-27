package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class DynaTokens(
  val canvasBg: Color = DynaPalette.CanvasBg,
  val surfaceCard: Color = DynaPalette.SurfaceCard,
  val surfaceRaised: Color = DynaPalette.SurfaceRaised,
  val surfaceBorder: Color = DynaPalette.SurfaceBorder,
  val borderSubtle: Color = DynaPalette.BorderSubtle,
  val textPrimary: Color = DynaPalette.TextPrimary,
  val textSecondary: Color = DynaPalette.TextSecondary,
  val textMuted: Color = DynaPalette.TextMuted,
  val accentCyan: Color = DynaPalette.AccentCyan,
  val accentIce: Color = DynaPalette.AccentIce,
  val accentGlow: Color = DynaPalette.AccentGlow,
  val statusOnline: Color = DynaPalette.StatusOnline,
  val bentoRadius: Dp = 8.dp,
  val controlRadius: Dp = 4.dp,
  val spaceXs: Dp = 4.dp,
  val spaceSm: Dp = 8.dp,
  val spaceMd: Dp = 16.dp,
  val spaceLg: Dp = 24.dp,
)

val LocalDynaTokens = staticCompositionLocalOf { DynaTokens() }

private val DynaDarkColorScheme =
  darkColorScheme(
    primary = DynaPalette.Primary,
    onPrimary = DynaPalette.OnPrimary,
    primaryContainer = DynaPalette.PrimaryContainer,
    onPrimaryContainer = DynaPalette.OnPrimaryContainer,
    secondary = DynaPalette.Secondary,
    onSecondary = DynaPalette.OnSecondary,
    secondaryContainer = DynaPalette.SecondaryContainer,
    onSecondaryContainer = DynaPalette.OnSecondaryContainer,
    tertiary = DynaPalette.Tertiary,
    onTertiary = DynaPalette.OnTertiary,
    tertiaryContainer = DynaPalette.TertiaryContainer,
    onTertiaryContainer = DynaPalette.OnTertiaryContainer,
    background = DynaPalette.CanvasBg,
    onBackground = DynaPalette.OnSurface,
    surface = DynaPalette.SurfaceCard,
    onSurface = DynaPalette.OnSurface,
    surfaceVariant = DynaPalette.SurfaceRaised,
    onSurfaceVariant = DynaPalette.OnSurfaceVariant,
    surfaceContainerLowest = DynaPalette.SurfaceContainerLowest,
    surfaceContainerLow = DynaPalette.SurfaceContainerLow,
    surfaceContainer = DynaPalette.SurfaceContainer,
    surfaceContainerHigh = DynaPalette.SurfaceContainerHigh,
    surfaceContainerHighest = DynaPalette.SurfaceContainerHighest,
    outline = DynaPalette.Outline,
    outlineVariant = DynaPalette.OutlineVariant,
    error = DynaPalette.Error,
    onError = DynaPalette.OnError,
    errorContainer = DynaPalette.ErrorContainer,
    onErrorContainer = DynaPalette.OnErrorContainer,
  )

val DynaShapes =
  Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun DynaTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalDynaTokens provides DynaTokens()) {
    MaterialTheme(
      colorScheme = DynaDarkColorScheme,
      typography = DynaTypography,
      shapes = DynaShapes,
      content = content,
    )
  }
}
