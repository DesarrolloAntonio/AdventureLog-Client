package com.desarrollodroide.adventurelog.feature.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.roboto_flex
import org.jetbrains.compose.resources.Font

// Roboto Flex: one variable file (SIL OFL 1.1, the licence travels in the font's name table), read
// at different weight and width settings. The axes need API 26+; on 24-25 the default instance is
// used and bold is synthesised.

/** The canvas's `font-stretch` values, as the font's width axis. */
private const val NormalWidth = 100f
private const val CondensedWidth = 38f
private const val StampWidth = 60f

@Composable
private fun flex(weight: Int, width: Float) = Font(
    Res.font.roboto_flex,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.width(width),
    ),
)

/** Normal width: titles, body and labels. */
@Composable
fun robotoFlex(): FontFamily = FontFamily(
    flex(400, NormalWidth),
    flex(500, NormalWidth),
    flex(600, NormalWidth),
    flex(700, NormalWidth),
    flex(800, NormalWidth),
)

/** Narrow and heavy: display and headline, the voice of the redesign ("Hi John, your next trip"). */
@Composable
fun robotoFlexCondensed(): FontFamily = FontFamily(
    flex(700, CondensedWidth),
    flex(800, CondensedWidth),
    flex(900, CondensedWidth),
)

@Composable
private fun robotoFlexStamp(): FontFamily = FontFamily(flex(800, StampWidth))

/**
 * A display line as the canvas draws it: weight 800, set at 0.95 of its size. Font-free, so it can
 * be checked without a composition; [AdventureType.display] adds the condensed family.
 */
fun DisplaySpec(size: TextUnit): TextStyle {
    require(size.isSp) { "Display sizes are in sp" }
    return TextStyle(
        fontWeight = FontWeight(800),
        fontSize = size,
        lineHeight = (size.value * 0.95f).sp,
        letterSpacing = 0.sp,
    )
}

/** A country code on a passport chip: "NO", "JP". */
val StampSpec: TextStyle = TextStyle(
    fontWeight = FontWeight(800),
    fontSize = 15.sp,
    letterSpacing = 1.sp,
)

/** The redesign's own voices, beyond Material's type scale. */
object AdventureType {
    @Composable
    fun display(size: TextUnit): TextStyle = DisplaySpec(size).copy(fontFamily = robotoFlexCondensed())

    @Composable
    fun stamp(): TextStyle = StampSpec.copy(fontFamily = robotoFlexStamp())
}
