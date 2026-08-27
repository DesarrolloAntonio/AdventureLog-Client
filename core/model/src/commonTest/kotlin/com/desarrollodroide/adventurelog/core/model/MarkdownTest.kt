package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownTest {

    @Test
    fun `the description that started this reads as three blocks`() {
        val blocks = parseMarkdown(
            """
            Castillo

            ---

            🚗 **Desde Leganés:** 5h 59min · 518 km
            """.trimIndent()
        )

        assertEquals(3, blocks.size)
        assertTrue(blocks[1] is MarkdownBlock.Rule)

        val last = blocks[2] as MarkdownBlock.Paragraph
        assertEquals("Desde Leganés:", last.spans.first { it.bold }.text)
        assertTrue(last.spans.none { it.text.contains("*") })
    }

    @Test
    fun `a rule needs three markers on a line of its own`() {
        assertTrue(parseMarkdown("---").single() is MarkdownBlock.Rule)
        assertTrue(parseMarkdown("***").single() is MarkdownBlock.Rule)
        // Two hyphens is a dash somebody typed, not a rule.
        assertTrue(parseMarkdown("--").single() is MarkdownBlock.Paragraph)
    }

    @Test
    fun `headings carry their level`() {
        val blocks = parseMarkdown("# Big\n## Smaller")
        assertEquals(1, (blocks[0] as MarkdownBlock.Paragraph).level)
        assertEquals(2, (blocks[1] as MarkdownBlock.Paragraph).level)
    }

    @Test
    fun `lists count themselves`() {
        val blocks = parseMarkdown("1. one\n2. two\n3. three")
        assertEquals(listOf(1, 2, 3), blocks.map { (it as MarkdownBlock.ListItem).index })
        assertTrue(blocks.all { (it as MarkdownBlock.ListItem).ordered })

        val bullets = parseMarkdown("- a\n* b\n+ c")
        assertEquals(3, bullets.size)
        assertTrue(bullets.none { (it as MarkdownBlock.ListItem).ordered })
    }

    @Test
    fun `a link keeps its text and its target`() {
        val span = parseSpans("see [the map](https://example.com/x) here").single { it.link != null }
        assertEquals("the map", span.text)
        assertEquals("https://example.com/x", span.link)
    }

    @Test
    fun `italic inside bold stays inside it`() {
        val spans = parseSpans("**a *b* c**")
        assertTrue(spans.first { it.text.trim() == "b" }.italic)
        assertTrue(spans.all { it.bold })
    }

    @Test
    fun `an unclosed marker is the character somebody typed`() {
        assertEquals("2 * 3 = 6", parseSpans("2 * 3 = 6").joinToString("") { it.text })
        assertEquals("a_b_c", parseSpans("a_b_c").joinToString("") { it.text }.let { it })
    }

    @Test
    fun `lines of one paragraph join with a space`() {
        val block = parseMarkdown("one\ntwo").single() as MarkdownBlock.Paragraph
        assertEquals("one two", block.spans.joinToString("") { it.text })
    }

    @Test
    fun `plain text survives untouched`() {
        val text = "Nacimiento Río mundo"
        val block = parseMarkdown(text).single() as MarkdownBlock.Paragraph
        assertEquals(text, block.spans.single().text)
    }
}
