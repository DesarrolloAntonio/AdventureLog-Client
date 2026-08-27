package com.desarrollodroide.adventurelog.core.model

/**
 * Just enough Markdown to read what people actually write in AdventureLog.
 *
 * The web renders descriptions as Markdown, so a description written there arrives full of
 * asterisks and dashes that a phone was printing literally - `**Desde Leganés:**` and a row of
 * hyphens where a rule should be. This turns the source into blocks and spans; drawing them is
 * the UI's business.
 *
 * Deliberately not a full implementation. Tables, block quotes, nested lists and reference links
 * do not appear in a travel journal, and every construct supported here is one that has.
 */
sealed interface MarkdownBlock {
    /** A run of text. [level] is 0 for a paragraph, 1-6 for a heading. */
    data class Paragraph(val spans: List<MarkdownSpan>, val level: Int = 0) : MarkdownBlock

    /** One item of a list. [ordered] prints a number, [index] being its position from 1. */
    data class ListItem(
        val spans: List<MarkdownSpan>,
        val ordered: Boolean,
        val index: Int
    ) : MarkdownBlock

    /** `---`, `***` or `___` on a line of its own. */
    data object Rule : MarkdownBlock
}

/**
 * A piece of a line, with whatever emphasis was asked for. A span with a [link] is tappable.
 */
data class MarkdownSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: String? = null
)

private val RULE = Regex("^\\s*([-*_])\\1{2,}\\s*$")
private val HEADING = Regex("^(#{1,6})\\s+(.*)$")
private val BULLET = Regex("^\\s*[-*+]\\s+(.*)$")
private val NUMBERED = Regex("^\\s*(\\d+)[.)]\\s+(.*)$")

fun parseMarkdown(source: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = mutableListOf<String>()
    var orderedIndex = 0

    fun flushParagraph() {
        if (paragraph.isEmpty()) return
        blocks += MarkdownBlock.Paragraph(parseSpans(paragraph.joinToString(" ")))
        paragraph.clear()
    }

    for (raw in source.lines()) {
        val line = raw.trimEnd()
        when {
            line.isBlank() -> {
                flushParagraph()
                orderedIndex = 0
            }

            RULE.matches(line) -> {
                flushParagraph()
                orderedIndex = 0
                blocks += MarkdownBlock.Rule
            }

            HEADING.matches(line) -> {
                flushParagraph()
                orderedIndex = 0
                val (hashes, text) = HEADING.find(line)!!.destructured
                blocks += MarkdownBlock.Paragraph(parseSpans(text), hashes.length)
            }

            NUMBERED.matches(line) -> {
                flushParagraph()
                orderedIndex++
                val (_, text) = NUMBERED.find(line)!!.destructured
                blocks += MarkdownBlock.ListItem(parseSpans(text), ordered = true, index = orderedIndex)
            }

            BULLET.matches(line) -> {
                flushParagraph()
                orderedIndex = 0
                val text = BULLET.find(line)!!.groupValues[1]
                blocks += MarkdownBlock.ListItem(parseSpans(text), ordered = false, index = 0)
            }

            else -> paragraph += line.trim()
        }
    }
    flushParagraph()
    return blocks
}

/**
 * Emphasis, code and links within one line.
 *
 * Written as a scanner rather than a set of regexes because the markers nest and overlap - `**a
 * *b* c**` is bold throughout with one italic word inside - and because an unclosed marker has to
 * survive as the literal asterisk somebody typed.
 */
internal fun parseSpans(line: String): List<MarkdownSpan> {
    val spans = mutableListOf<MarkdownSpan>()
    val text = StringBuilder()
    var bold = false
    var italic = false
    var i = 0

    fun flush() {
        if (text.isNotEmpty()) {
            spans += MarkdownSpan(text.toString(), bold = bold, italic = italic)
            text.clear()
        }
    }

    while (i < line.length) {
        val rest = line.substring(i)
        when {
            rest.startsWith("**") && rest.indexOf("**", 2) > 0 -> {
                flush(); bold = !bold; i += 2
            }

            rest.startsWith("**") && bold -> {
                flush(); bold = false; i += 2
            }

            (rest.startsWith("*") || rest.startsWith("_")) &&
                opensEmphasis(line, i) && hasCloser(rest, rest.first().toString()) -> {
                flush(); italic = !italic; i += 1
            }

            (rest.startsWith("*") || rest.startsWith("_")) && italic -> {
                flush(); italic = false; i += 1
            }

            rest.startsWith("`") -> {
                val end = rest.indexOf('`', 1)
                if (end > 0) {
                    flush()
                    spans += MarkdownSpan(rest.substring(1, end), code = true)
                    i += end + 1
                } else {
                    text.append('`'); i++
                }
            }

            rest.startsWith("[") -> {
                val close = rest.indexOf(']')
                val open = if (close > 0) rest.getOrNull(close + 1) else null
                val end = if (open == '(') rest.indexOf(')', close) else -1
                if (close > 0 && end > 0) {
                    flush()
                    spans += MarkdownSpan(
                        text = rest.substring(1, close),
                        bold = bold,
                        italic = italic,
                        link = rest.substring(close + 2, end)
                    )
                    i += end + 1
                } else {
                    text.append('['); i++
                }
            }

            else -> {
                text.append(line[i]); i++
            }
        }
    }
    flush()
    return spans
}

/** True when the marker opened here is closed again later on the same line. */
private fun hasCloser(rest: String, marker: String): Boolean =
    rest.indexOf(marker, 1).let { it > 0 && rest.substring(1, it).isNotBlank() }

/**
 * An underscore between letters is part of a word, not emphasis - `snake_case_name` is a name.
 * Markdown says the same, and it is the only reason a file name survives being written down.
 */
private fun opensEmphasis(line: String, index: Int): Boolean =
    line[index] != '_' || index == 0 || !line[index - 1].isLetterOrDigit()
