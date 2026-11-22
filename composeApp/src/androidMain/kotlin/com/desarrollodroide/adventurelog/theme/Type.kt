package com.desarrollodroide.adventurelog.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.robotoFlex
import com.desarrollodroide.adventurelog.feature.ui.theme.robotoFlexCondensed

/**
 * Material's type scale on Roboto Flex, as the Expressive canvas (`design/native-ui/`) sets it:
 * display and headline condensed and heavy, everything else at normal width.
 *
 * Sizes are Material's own. Quicksand needed the small end raised a step for its small x-height;
 * Roboto Flex has a normal one, so the stock numbers read at their stated size.
 */
@Composable
fun getAppTypography(): Typography {
    val text = robotoFlex()
    val display = robotoFlexCondensed()

    fun style(family: FontFamily, weight: Int, size: Int, lineHeight: Int, tracking: Double = 0.0) =
        TextStyle(
            fontFamily = family,
            fontWeight = FontWeight(weight),
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = tracking.sp,
        )

    return Typography(
        displayLarge = style(display, 800, 57, 60, -0.25),
        displayMedium = style(display, 800, 45, 48),
        displaySmall = style(display, 800, 36, 40),
        headlineLarge = style(display, 800, 32, 36),
        headlineMedium = style(display, 800, 28, 32),
        headlineSmall = style(display, 800, 24, 28),
        titleLarge = style(text, 600, 22, 28),
        titleMedium = style(text, 600, 16, 24, 0.15),
        titleSmall = style(text, 600, 14, 20, 0.1),
        bodyLarge = style(text, 400, 16, 24, 0.5),
        bodyMedium = style(text, 400, 14, 20, 0.25),
        bodySmall = style(text, 400, 12, 16, 0.4),
        labelLarge = style(text, 600, 14, 20, 0.1),
        labelMedium = style(text, 600, 12, 16, 0.5),
        labelSmall = style(text, 600, 11, 16, 0.5),
        displayLargeEmphasized = style(display, 900, 57, 60, -0.25),
        displayMediumEmphasized = style(display, 900, 45, 48),
        displaySmallEmphasized = style(display, 900, 36, 40),
        headlineLargeEmphasized = style(display, 900, 32, 36),
        headlineMediumEmphasized = style(display, 900, 28, 32),
        headlineSmallEmphasized = style(display, 900, 24, 28),
        titleLargeEmphasized = style(text, 700, 22, 28),
        titleMediumEmphasized = style(text, 700, 16, 24, 0.15),
        titleSmallEmphasized = style(text, 700, 14, 20, 0.1),
        bodyLargeEmphasized = style(text, 600, 16, 24, 0.5),
        bodyMediumEmphasized = style(text, 600, 14, 20, 0.25),
        bodySmallEmphasized = style(text, 600, 12, 16, 0.4),
        labelLargeEmphasized = style(text, 700, 14, 20, 0.1),
        labelMediumEmphasized = style(text, 700, 12, 16, 0.5),
        labelSmallEmphasized = style(text, 700, 11, 16, 0.5),
    )
}
