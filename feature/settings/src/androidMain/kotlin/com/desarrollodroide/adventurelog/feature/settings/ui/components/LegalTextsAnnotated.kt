package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** [plainText] with the headings in bold, for the Terms of Use and Privacy Policy screens. */
internal fun LegalText.annotated(): AnnotatedString = buildAnnotatedString {
    append("$effective\n\n")
    sections.forEach { section ->
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${section.heading}\n\n") }
        append("${section.body}\n\n")
    }
}
