package com.desarrollodroide.adventurelog.feature.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import android.content.res.AssetManager
import androidx.compose.ui.platform.LocalContext

// Roboto Flex: one variable file (SIL OFL 1.1, the licence travels in the font's name table), read
// at different weight and width settings. The axes need API 26+; on 24-25 the default instance is
// used and bold is synthesised.
//
// Optical size matters as much as width. A browser sets Roboto Flex's opsz axis to the font size
// on its own (font-optical-sizing: auto), and the narrow cuts only open up at large opsz: width 38
// is 81% of width 100 at opsz 44 but 92% at the default opsz 14. Android leaves opsz at 14, so
// the canvas's condensed display only appears when opsz is set to the size, as [condensed] does.
// ExpressivePiecesTest.condensedDisplayIsReallyNarrower holds it.

/** The canvas's `font-stretch` values, as the font's width axis. */
private const val NormalWidth = 100f
private const val CondensedWidth = 38f
private const val StampWidth = 60f

private fun flex(assets: AssetManager, weight: Int, width: Float, opticalSize: TextUnit? = null) = Font(
    path = "fonts/roboto_flex.ttf",
    assetManager = assets,
    weight = FontWeight(weight),
    variationSettings = if (opticalSize == null) {
        FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width))
    } else {
        FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.width(width),
            FontVariation.opticalSizing(opticalSize),
        )
    },
)

/** Normal width: titles, body and labels. */
@Composable
fun robotoFlex(): FontFamily {
    val assets = LocalContext.current.assets
    return remember(assets) {
        FontFamily(
            flex(assets, 400, NormalWidth),
            flex(assets, 500, NormalWidth),
            flex(assets, 600, NormalWidth),
            flex(assets, 700, NormalWidth),
            flex(assets, 800, NormalWidth),
        )
    }
}

/**
 * Narrow and heavy: display and headline, the voice of the redesign ("Hi John, your next trip"),
 * cut for text of [size] - see the note on optical size above.
 */
@Composable
fun robotoFlexCondensed(size: TextUnit): FontFamily {
    val assets = LocalContext.current.assets
    return remember(assets, size) {
        FontFamily(
            flex(assets, 700, CondensedWidth, size),
            flex(assets, 800, CondensedWidth, size),
            flex(assets, 900, CondensedWidth, size),
        )
    }
}

@Composable
private fun robotoFlexStamp(): FontFamily {
    val assets = LocalContext.current.assets
    return remember(assets) { FontFamily(flex(assets, 800, StampWidth, StampSpec.fontSize)) }
}

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
    fun display(size: TextUnit): TextStyle = DisplaySpec(size).copy(fontFamily = robotoFlexCondensed(size))

    @Composable
    fun stamp(): TextStyle = StampSpec.copy(fontFamily = robotoFlexStamp())
}
