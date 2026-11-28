package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val GeistFontFamily =
  FontFamily(
    Font(R.font.geist, FontWeight.Normal),
    Font(R.font.geist, FontWeight.Medium),
    Font(R.font.geist, FontWeight.SemiBold),
  )

val InterFontFamily =
  FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
  )

val JetBrainsMonoFontFamily =
  FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
  )

object DynaMonoTypography {
  val LabelMono =
    TextStyle(
      fontFamily = JetBrainsMonoFontFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 15.6.sp,
      letterSpacing = 0.36.sp,
      fontFeatureSettings = "tnum",
    )

  val LabelMonoCaps = LabelMono

  val LabelCode =
    TextStyle(
      fontFamily = JetBrainsMonoFontFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 15.6.sp,
      letterSpacing = 0.36.sp,
      fontFeatureSettings = "tnum",
    )

  val TabularTimer =
    TextStyle(
      fontFamily = JetBrainsMonoFontFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 20.sp,
      lineHeight = 24.sp,
      letterSpacing = (-0.2).sp,
      fontFeatureSettings = "tnum",
    )
}

val DynaTypography =
  Typography(
    // headline-xl: 2rem (32sp)
    displayLarge =
      TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.8.sp,
        letterSpacing = (-0.8).sp,
      ),
    // headline-lg: 1.5rem (24sp)
    headlineLarge =
      TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = (-0.48).sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp,
        letterSpacing = (-0.48).sp,
      ),
    // headline-md: 1.125rem (18sp)
    titleLarge =
      TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 23.4.sp,
        letterSpacing = (-0.27).sp,
      ),
    // headline-sm: 0.9375rem (15sp)
    titleMedium =
      TextStyle(
        fontFamily = GeistFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.25.sp,
        letterSpacing = (-0.15).sp,
      ),
    // body-lg: 0.9375rem (15sp)
    bodyLarge =
      TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.5.sp,
        letterSpacing = (-0.075).sp,
      ),
    // body-md: 0.875rem (14sp)
    bodyMedium =
      TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.3.sp,
        letterSpacing = 0.sp,
      ),
    // body-sm: 0.75rem (12sp)
    bodySmall =
      TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.sp,
      ),
    labelMedium = DynaMonoTypography.LabelMono,
    labelSmall = DynaMonoTypography.LabelMono,
  )
