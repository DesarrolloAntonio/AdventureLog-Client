package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.MarkdownBlock
import com.desarrollodroide.adventurelog.core.model.MarkdownSpan
import com.desarrollodroide.adventurelog.core.model.parseMarkdown

/**
 * Text written in Markdown, drawn as Markdown.
 *
 * Descriptions are authored on the web, which renders them - so a phone printing the source was
 * showing people the punctuation they meant to be invisible: `**Desde Leganés:**` in asterisks and
 * a row of hyphens where a rule belonged.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = parseMarkdown(markdown)

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(Modifier.height(if (block is MarkdownBlock.Rule) 12.dp else 8.dp))
            when (block) {
                is MarkdownBlock.Rule -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                is MarkdownBlock.Paragraph -> Text(
                    text = block.spans.toAnnotated(),
                    style = headingStyle(block.level, style),
                    color = color
                )

                is MarkdownBlock.ListItem -> Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (block.ordered) "${block.index}." else "•",
                        style = style,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = block.spans.toAnnotated(),
                        style = style,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Headings step down from the body size rather than up from a display size, so a `#` inside a
 *  card does not shout over the section it sits in. */
@Composable
private fun headingStyle(level: Int, base: TextStyle): TextStyle = when (level) {
    0 -> base
    1 -> base.copy(fontSize = base.fontSize * 1.4f, fontWeight = FontWeight.Bold)
    2 -> base.copy(fontSize = base.fontSize * 1.25f, fontWeight = FontWeight.Bold)
    3 -> base.copy(fontSize = base.fontSize * 1.1f, fontWeight = FontWeight.Bold)
    else -> base.copy(fontWeight = FontWeight.Bold)
}

@Composable
private fun List<MarkdownSpan>.toAnnotated(): AnnotatedString {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant

    return buildAnnotatedString {
        forEach { span ->
            val style = SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                fontFamily = if (span.code) FontFamily.Monospace else null,
                background = if (span.code) codeBackground else Color.Unspecified,
                fontSize = if (span.code) 14.sp else androidx.compose.ui.unit.TextUnit.Unspecified
            )
            val url = span.link
            if (url != null) {
                withLink(LinkAnnotation.Url(url = url, styles = linkStyles)) {
                    withStyle(style) { append(span.text) }
                }
            } else {
                withStyle(style) { append(span.text) }
            }
        }
    }
}
