package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

/** Visible UTF-16 units retain independent marks, links and immutable Artemis payloads. */
internal data class MarkdownGlyph(
    val char: Char,
    val marks: Set<MarkdownStyle> = emptySet(),
    val link: String? = null,
    val atom: MarkdownAtom? = null,
    val codeInfo: String = "",
    val verbatim: Boolean = false,
)
internal data class MarkdownAtom(val source: String, val label: String)

internal object MarkdownDocument {
    val inlineStyles = setOf(MarkdownStyle.Bold, MarkdownStyle.Italic, MarkdownStyle.Underline, MarkdownStyle.Strikethrough, MarkdownStyle.InlineCode)
    fun glyphs(presentation: MarkdownPresentation): List<MarkdownGlyph> {
        val starts = mutableMapOf<Int, MutableList<MarkdownPresentation.Span>>()
        val ends = mutableMapOf<Int, MutableList<MarkdownPresentation.Span>>()
        val atoms = mutableMapOf<MarkdownPresentation.Span, MarkdownAtom>()
        for (span in presentation.spans) {
            val range = presentation.visibleRange(span)
            if (range.collapsed) continue
            starts.getOrPut(range.min) { mutableListOf() } += span
            ends.getOrPut(range.max) { mutableListOf() } += span
            if (span.isAtomic) atoms[span] = MarkdownAtom(presentation.source.substring(span.start, span.end), presentation.text.substring(range.min, range.max))
        }
        val active = linkedSetOf<MarkdownPresentation.Span>()
        val quoteEnds = presentation.spans.filter { it.style == MarkdownStyle.Blockquote }.map { presentation.visibleRange(it).max }.toSet()
        var marks = emptySet<MarkdownStyle>()
        var link: String? = null
        var atom: MarkdownAtom? = null
        var info = ""
        var raw = false
        return presentation.text.mapIndexed { index, char ->
            if (starts.containsKey(index) || ends.containsKey(index)) {
                ends[index]?.let { active.removeAll(it.toSet()) }
                starts[index]?.let { active.addAll(it) }
                marks = active.mapNotNull { it.style }.toSet()
                link = active.firstOrNull { it.kind == MarkdownPresentation.Kind.LINK }?.destination
                atom = active.firstNotNullOfOrNull { atoms[it] }
                info = active.firstOrNull { it.style == MarkdownStyle.CodeBlock }?.codeInfo.orEmpty()
                raw = active.any { it.kind == MarkdownPresentation.Kind.RAW }
            }
            MarkdownGlyph(char, if (char == '\n' && index in quoteEnds) marks + MarkdownStyle.Blockquote else marks, link, atom, info, raw)
        }
    }

    fun encode(glyphs: List<MarkdownGlyph>): String = buildString {
        var index = 0
        while (index < glyphs.size) {
            if (MarkdownStyle.CodeBlock in glyphs[index].marks) {
                val start = index
                while (index < glyphs.size && MarkdownStyle.CodeBlock in glyphs[index].marks) index++
                val body = glyphs.subList(start, index).joinToString("") { it.char.toString() }
                val fence = "`".repeat(maxOf(3, (Regex("`+").findAll(body).maxOfOrNull { it.value.length } ?: 0) + 1))
                append(fence).append(glyphs[start].codeInfo).append('\n').append(body).append('\n').append(fence)
            } else {
                val start = index
                while (index < glyphs.size && glyphs[index].char != '\n' && MarkdownStyle.CodeBlock !in glyphs[index].marks) index++
                val line = glyphs.subList(start, index)
                val text = line.joinToString("") { it.char.toString() }
                val quote = line.any { MarkdownStyle.Blockquote in it.marks } ||
                    line.isEmpty() && index < glyphs.size && MarkdownStyle.Blockquote in glyphs[index].marks
                val list = line.any { g -> g.marks.any { it in setOf(MarkdownStyle.UnorderedList, MarkdownStyle.OrderedList) } }
                val prefix = if (list) Regex("^(?:• |[-+*] |\\d+\\. )").find(text)?.value.orEmpty() else ""
                append(when { quote -> "> "; prefix.startsWith("•") -> "- "; else -> prefix })
                append(encodeLinks(line.drop(prefix.length)))
            }
            if (index < glyphs.size && glyphs[index].char == '\n') { append('\n'); index++ }
        }
    }

    private fun encodeLinks(glyphs: List<MarkdownGlyph>): String = buildString {
        var index = 0
        while (index < glyphs.size) {
            val start = index
            val link = glyphs[index].link
            while (index < glyphs.size && glyphs[index].link == link) index++
            val content = encodeEmphasis(glyphs.subList(start, index))
            if (link == null) append(content) else append('[').append(content).append("](").append(escapeDestination(link)).append(')')
        }
    }
    fun escapeDestination(text: String) = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
    private fun text(glyphs: List<MarkdownGlyph>) = glyphs.joinToString("") { it.char.toString() }
    private fun wrap(content: String, open: String, close: String): String {
        if (content.isBlank()) return content
        val leading = content.takeWhile(Char::isWhitespace)
        val trailing = content.takeLastWhile(Char::isWhitespace)
        return leading + open + content.trim() + close + trailing
    }

    private fun encodeEmphasis(glyphs: List<MarkdownGlyph>): String {
        if (glyphs.isEmpty()) return ""
        val groups = mutableListOf<MutableList<MarkdownGlyph>>()
        fun key(g: MarkdownGlyph) = (if (MarkdownStyle.Bold in g.marks) 1 else 0) + (if (MarkdownStyle.Italic in g.marks) 2 else 0)
        for (glyph in glyphs) {
            if (groups.lastOrNull()?.lastOrNull()?.let(::key) == key(glyph)) groups.last().add(glyph) else groups += mutableListOf(glyph)
        }
        data class State(val full: String, val beforeLast: String, val last: String, val glyphs: List<MarkdownGlyph>)
        var states = listOf(State("", "", "", emptyList()))
        for (group in groups) {
            val body = encodeBody(group, 0)
            val delimiters = when (key(group.first())) {
                1 -> listOf("**" to "**", "__" to "__")
                2 -> listOf("*" to "*", "_" to "_")
                3 -> listOf("***" to "***", "___" to "___", "**_" to "_**", "__*" to "*__", "*__" to "__*", "_**" to "**_")
                else -> listOf("" to "")
            }
            val next = linkedMapOf<String, State>()
            for (previous in states) for ((open, close) in delimiters) {
                val option = wrap(body, open, close)
                // Delimiter flanking depends on the adjacent runs and preceding character class.
                // Validate that bounded boundary, rather than reparsing every growing prefix.
                val last = previous.beforeLast.lastOrNull()
                val guard = when { last == null -> ""; last.isWhitespace() -> " "; last.isLetterOrDigit() -> "q"; else -> "!" }
                val guardSource = if (guard == "!") "\\!" else guard
                val expected = guard.map { MarkdownGlyph(it) } + previous.glyphs + group
                if (matches(guardSource + previous.last + option, expected)) {
                    val full = previous.full + option
                    val tail = full.takeLastWhile { it == '*' || it == '_' }
                    next.putIfAbsent(tail, State(full, previous.full, option, group))
                }
            }
            if (next.isEmpty()) return encodeHtml(glyphs)
            states = next.values.take(12)
        }
        return states.firstOrNull { matches(it.full, glyphs) }?.full ?: encodeHtml(glyphs)
    }

    private fun matches(source: String, expected: List<MarkdownGlyph>): Boolean {
        val actual = glyphs(MarkdownPresentation.parse(source))
        if (text(actual) != text(expected)) return false
        return actual.zip(expected).all { (a, b) -> a.char.isWhitespace() || a.marks.intersect(inlineStyles) == b.marks.intersect(inlineStyles) }
    }
    private val bodyMarks = listOf(MarkdownStyle.Underline to ("<ins>" to "</ins>"), MarkdownStyle.Strikethrough to ("~~" to "~~"))
    private fun encodeBody(glyphs: List<MarkdownGlyph>, level: Int): String {
        if (level >= bodyMarks.size) return encodeCode(glyphs)
        val (mark, tags) = bodyMarks[level]
        return buildString {
            var index = 0
            while (index < glyphs.size) {
                val start = index
                val active = mark in glyphs[index].marks
                while (index < glyphs.size && (mark in glyphs[index].marks) == active) index++
                val body = encodeBody(glyphs.subList(start, index), level + 1)
                append(if (active) wrap(body, tags.first, tags.second) else body)
            }
        }
    }
    private fun encodeCode(glyphs: List<MarkdownGlyph>): String = buildString {
        var index = 0
        while (index < glyphs.size) {
            val atom = glyphs[index].atom
            if (atom != null) {
                append(atom.source)
                index += atom.label.length
                continue
            }
            val start = index
            val code = MarkdownStyle.InlineCode in glyphs[index].marks
            while (index < glyphs.size && glyphs[index].atom == null && (MarkdownStyle.InlineCode in glyphs[index].marks) == code) index++
            val body = text(glyphs.subList(start, index))
            if (code) {
                val fence = "`".repeat((Regex("`+").findAll(body).maxOfOrNull { it.value.length } ?: 0) + 1)
                val padding = if (body.startsWith('`') || body.endsWith('`') || body.startsWith(' ') && body.endsWith(' ') && body.isNotBlank()) " " else ""
                append(fence).append(padding).append(body).append(padding).append(fence)
            } else {
                for (glyph in glyphs.subList(start, index)) {
                    if (!glyph.verbatim && glyph.char in "\\`*_[]<>~&$#!+\\-=.") append('\\')
                    append(glyph.char)
                }
            }
        }
    }
    private fun encodeHtml(glyphs: List<MarkdownGlyph>): String = buildString {
        var index = 0
        while (index < glyphs.size) {
            val start = index
            val marks = glyphs[index].marks
            while (index < glyphs.size && glyphs[index].marks == marks) index++
            val part = glyphs.subList(start, index)
            val tags = listOf(MarkdownStyle.Bold to "strong", MarkdownStyle.Italic to "em").filter { it.first in marks }
            tags.forEach { append('<').append(it.second).append('>') }
            append(encodeBody(part, 0))
            tags.asReversed().forEach { append("</").append(it.second).append('>') }
        }
    }
}
