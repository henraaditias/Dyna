package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage

@Immutable
data class DynaTokens(
  val isDark: Boolean = true,
  val canvasBg: Color = DynaPalette.DarkCanvasBg,
  val surfaceCard: Color = DynaPalette.DarkSurfaceCard,
  val surfaceRaised: Color = DynaPalette.DarkSurfaceRaised,
  val surfaceInset: Color = DynaPalette.DarkSurfaceInset,
  val borderHairline: Color = DynaPalette.DarkBorderHairline,
  val borderStrong: Color = DynaPalette.DarkBorderStrong,
  val textPrimary: Color = DynaPalette.DarkTextPrimary,
  val textSecondary: Color = DynaPalette.DarkTextSecondary,
  val textMuted: Color = DynaPalette.DarkTextMuted,
  val accentPrimary: Color = DynaPalette.DarkAccentPrimary,
  val onAccentPrimary: Color = DynaPalette.DarkOnAccentPrimary,
  val statusActive: Color = DynaPalette.DarkStatusActive,
  val statusMuted: Color = DynaPalette.DarkStatusMuted,
  val statusDanger: Color = DynaPalette.DarkStatusDanger,
  val radiusSm: Dp = 8.dp,
  val radiusMd: Dp = 12.dp,
  val radiusLg: Dp = 16.dp,
  val radiusXl: Dp = 22.dp,
  val spaceXs: Dp = 4.dp,
  val spaceSm: Dp = 8.dp,
  val spaceMd: Dp = 16.dp,
  val spaceLg: Dp = 24.dp,
  val bottomNavClearance: Dp = 104.dp,
) {
  companion object {
    val Dark = DynaTokens(isDark = true)

    val Light =
      DynaTokens(
        isDark = false,
        canvasBg = DynaPalette.LightCanvasBg,
        surfaceCard = DynaPalette.LightSurfaceCard,
        surfaceRaised = DynaPalette.LightSurfaceRaised,
        surfaceInset = DynaPalette.LightSurfaceInset,
        borderHairline = DynaPalette.LightBorderHairline,
        borderStrong = DynaPalette.LightBorderStrong,
        textPrimary = DynaPalette.LightTextPrimary,
        textSecondary = DynaPalette.LightTextSecondary,
        textMuted = DynaPalette.LightTextMuted,
        accentPrimary = DynaPalette.LightAccentPrimary,
        onAccentPrimary = DynaPalette.LightOnAccentPrimary,
        statusActive = DynaPalette.LightStatusActive,
        statusMuted = DynaPalette.LightStatusMuted,
        statusDanger = DynaPalette.LightStatusDanger,
      )
  }
}

val LocalDynaTokens = staticCompositionLocalOf { DynaTokens.Dark }

private val DynaDarkColorScheme =
  darkColorScheme(
    primary = DynaPalette.DarkAccentPrimary,
    onPrimary = DynaPalette.DarkOnAccentPrimary,
    primaryContainer = DynaPalette.DarkSurfaceRaised,
    onPrimaryContainer = DynaPalette.DarkTextPrimary,
    secondary = DynaPalette.DarkTextSecondary,
    onSecondary = DynaPalette.DarkOnAccentPrimary,
    secondaryContainer = DynaPalette.DarkSurfaceRaised,
    onSecondaryContainer = DynaPalette.DarkTextPrimary,
    background = DynaPalette.DarkCanvasBg,
    onBackground = DynaPalette.DarkTextPrimary,
    surface = DynaPalette.DarkSurfaceCard,
    onSurface = DynaPalette.DarkTextPrimary,
    surfaceVariant = DynaPalette.DarkSurfaceRaised,
    onSurfaceVariant = DynaPalette.DarkTextSecondary,
    surfaceContainerLowest = DynaPalette.DarkSurfaceInset,
    surfaceContainerLow = DynaPalette.DarkSurfaceCard,
    surfaceContainer = DynaPalette.DarkSurfaceCard,
    surfaceContainerHigh = DynaPalette.DarkSurfaceRaised,
    surfaceContainerHighest = DynaPalette.DarkBorderHairline,
    outline = DynaPalette.DarkBorderStrong,
    outlineVariant = DynaPalette.DarkBorderHairline,
    error = DynaPalette.DarkStatusDanger,
    onError = DynaPalette.DarkOnAccentPrimary,
  )

private val DynaLightColorScheme =
  lightColorScheme(
    primary = DynaPalette.LightAccentPrimary,
    onPrimary = DynaPalette.LightOnAccentPrimary,
    primaryContainer = DynaPalette.LightSurfaceRaised,
    onPrimaryContainer = DynaPalette.LightTextPrimary,
    secondary = DynaPalette.LightTextSecondary,
    onSecondary = DynaPalette.LightOnAccentPrimary,
    secondaryContainer = DynaPalette.LightSurfaceRaised,
    onSecondaryContainer = DynaPalette.LightTextPrimary,
    background = DynaPalette.LightCanvasBg,
    onBackground = DynaPalette.LightTextPrimary,
    surface = DynaPalette.LightSurfaceCard,
    onSurface = DynaPalette.LightTextPrimary,
    surfaceVariant = DynaPalette.LightSurfaceRaised,
    onSurfaceVariant = DynaPalette.LightTextSecondary,
    surfaceContainerLowest = DynaPalette.LightSurfaceCard,
    surfaceContainerLow = DynaPalette.LightCanvasBg,
    surfaceContainer = DynaPalette.LightSurfaceRaised,
    surfaceContainerHigh = DynaPalette.LightSurfaceInset,
    surfaceContainerHighest = DynaPalette.LightBorderHairline,
    outline = DynaPalette.LightBorderStrong,
    outlineVariant = DynaPalette.LightBorderHairline,
    error = DynaPalette.LightStatusDanger,
    onError = DynaPalette.LightOnAccentPrimary,
  )

val DynaShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(999.dp),
  )

@Composable
fun DynaTheme(
  darkTheme: Boolean = true,
  language: AppLanguage = AppLanguage.ID,
  content: @Composable () -> Unit,
) {
  val tokens = if (darkTheme) DynaTokens.Dark else DynaTokens.Light
  val strings = stringsFor(language)
  val colorScheme = if (darkTheme) DynaDarkColorScheme else DynaLightColorScheme

  CompositionLocalProvider(
    LocalDynaTokens provides tokens,
    LocalDynaStrings provides strings,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = DynaTypography,
      shapes = DynaShapes,
      content = content,
    )
  }
}
