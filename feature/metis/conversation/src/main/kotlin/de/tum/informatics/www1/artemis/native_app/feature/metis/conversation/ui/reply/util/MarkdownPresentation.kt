package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import org.intellij.markdown.MarkdownElementTypes as E
import org.intellij.markdown.MarkdownTokenTypes as T
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.commonmark.internal.util.Escaping
import org.intellij.markdown.parser.MarkdownParser

/** Source positions come from the CommonMark/GFM parser, never delimiter searches. */
internal class MarkdownPresentation private constructor(
    val source: String,
    val text: String,
    val spans: List<Span>,
    val offsetMapping: OffsetMapping,
) {
    enum class Kind { STYLE, LINK, REFERENCE, IMAGE, ATTACHMENT, RAW }
    data class Span(
        val start: Int, val contentStart: Int, val contentEnd: Int, val end: Int,
        val style: MarkdownStyle? = null, val destination: String? = null,
        val kind: Kind = Kind.STYLE, val codeInfo: String = "",
    ) {
        val isAtomic get() = kind in setOf(Kind.REFERENCE, Kind.IMAGE, Kind.ATTACHMENT)
    }
    fun visibleRange(span: Span) = TextRange(
        offsetMapping.originalToTransformed(if (span.isAtomic) span.start else span.contentStart),
        offsetMapping.originalToTransformed(if (span.isAtomic) span.end else span.contentEnd),
    )
    val hasContent get() = spans.any { it.isAtomic } || text.lineSequence().any {
        it.replace(Regex("^\\s*(?:[>│] |[-+*•] |\\d+\\. )"), "").isNotBlank()
    } || spans.any { it.style in setOf(MarkdownStyle.InlineCode, MarkdownStyle.CodeBlock) && text.substring(visibleRange(it).min, visibleRange(it).max).isNotBlank() }

    companion object {
        private val reference = Regex("\\[(user|channel|text|quiz|lecture|modeling|file-upload|programming|attachment|lecture-unit|slide|faq|message|post|answer)]([^\\n]*?)\\(([^\\n]*?)\\)\\[/\\1]")
        private val htmlTag = Regex("</?(ins|u|strong|b|em|i|s|del|code)>", RegexOption.IGNORE_CASE)
        private val escapedOrEntity = Regex("\\\\[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]\\\\^_`{|}~]|&(?:#[0-9]+|#x[0-9a-fA-F]+|[A-Za-z][A-Za-z0-9]+);")
        private val htmlStyles = mapOf("ins" to MarkdownStyle.Underline, "u" to MarkdownStyle.Underline,
            "strong" to MarkdownStyle.Bold, "b" to MarkdownStyle.Bold, "em" to MarkdownStyle.Italic,
            "i" to MarkdownStyle.Italic, "s" to MarkdownStyle.Strikethrough, "del" to MarkdownStyle.Strikethrough, "code" to MarkdownStyle.InlineCode)
        private data class Replacement(val start: Int, val end: Int, val text: String)
        // Use the same CommonMark escape/entity decoder as Markwon's message renderer.
        fun unescape(text: String): String = Escaping.unescapeString(text)

        fun parse(source: String, selection: TextRange? = null): MarkdownPresentation {
            val hidden = BooleanArray(source.length)
            val opening = BooleanArray(source.length)
            val literal = BooleanArray(source.length)
            val raw = BooleanArray(source.length)
            val spans = mutableListOf<Span>()
            val atomicSpans = mutableListOf<Span>()
            val replacements = mutableMapOf<Int, Replacement>()
            fun hide(from: Int, to: Int, opens: Boolean) {
                for (i in from until to) { hidden[i] = true; opening[i] = opens }
            }
            fun add(span: Span) {
                spans += span
                if (span.isAtomic) atomicSpans += span
                hide(span.start, span.contentStart, true)
                hide(span.contentEnd, span.end, false)
            }
            val masked = source.toCharArray()
            val originalTree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(source)
            val protected = mutableListOf<IntRange>()
            val linkStarts = mutableMapOf<Int, IntRange>()
            fun protect(node: ASTNode) {
                if (node.type == E.INLINE_LINK) linkStarts[node.startOffset] = node.startOffset until node.endOffset
                if (node.type == E.IMAGE) linkStarts[node.startOffset + 1] = node.startOffset until node.endOffset
                if (node.type in setOf(E.CODE_SPAN, E.CODE_FENCE, E.CODE_BLOCK, E.HTML_BLOCK)) {
                    protected += node.startOffset until node.endOffset
                    return
                }
                node.children.forEach(::protect)
            }
            protect(originalTree)
            // Inline HTML code is also literal; mask neither its contents nor references.
            var hasMaskedContent = false
            Regex("<code>.*?</code>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .findAll(source).forEach { match ->
                    if (protected.any { match.range.first in it }) return@forEach
                    protected += match.range
                    // Keep tags and offsets, while preventing Markdown spans crossing literal code.
                    for (i in match.range.first + 6 until match.range.last - 6) if (!masked[i].isWhitespace()) masked[i] = 'r'
                    hasMaskedContent = true
                }
            var hasReferences = false
            var referenceOffset = 0
            while (referenceOffset < source.length) {
                val match = reference.find(source, referenceOffset) ?: break
                referenceOffset = match.range.last + 1
                val protectedRange = protected.firstOrNull { match.range.first in it } ?: linkStarts[match.range.first]
                if (protectedRange != null) {
                    // A link label can equal an Artemis tag. Skip only that link,
                    // not a greedy match that also consumed a later reference.
                    referenceOffset = protectedRange.last + 1
                    continue
                }
                var slashes = 0
                var before = match.range.first - 1
                while (before >= 0 && source[before--] == '\\') slashes++
                if (slashes % 2 != 0) {
                    referenceOffset = match.range.first + 1
                    continue
                }
                val label = match.groups[2]!!
                add(Span(match.range.first, label.range.first, label.range.last + 1, match.range.last + 1,
                    destination = "artemis:${match.groupValues[1]}", kind = Kind.REFERENCE))
                hasReferences = true
                for (i in match.range) masked[i] = if (i == match.range.first) '[' else if (i == match.range.last) ']' else 'r'
            }
            val tree = if (hasReferences || hasMaskedContent) MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(String(masked)) else originalTree
            val htmlStack = mutableListOf<Pair<String, IntRange>>()
            fun walk(node: ASTNode) {
                val a = node.startOffset; val b = node.endOffset
                if (atomicSpans.any { a >= it.start && b <= it.end }) return
                fun descendants(n: ASTNode): Sequence<ASTNode> = sequence { for (child in n.children) { yield(child); yieldAll(descendants(child)) } }
                if (node.type in setOf(E.UNORDERED_LIST, E.ORDERED_LIST, E.BLOCK_QUOTE) && descendants(node).any {
                        it.type in setOf(E.UNORDERED_LIST, E.ORDERED_LIST, E.BLOCK_QUOTE)
                    }) {
                    spans += Span(a, a, b, b, kind = Kind.RAW)
                    for (i in a until b) raw[i] = true
                    return
                }
                when (node.type) {
                    E.STRONG, E.EMPH, GFMElementTypes.STRIKETHROUGH -> {
                        val size = if (node.type == E.EMPH) 1 else 2
                        val style = when (node.type) { E.STRONG -> MarkdownStyle.Bold; E.EMPH -> MarkdownStyle.Italic; else -> MarkdownStyle.Strikethrough }
                        add(Span(a, a + size, b - size, b, style))
                    }
                    E.CODE_SPAN -> {
                        val ticks = node.children.filter { it.type == T.BACKTICK }
                        if (ticks.size >= 2) {
                            var start = ticks.first().endOffset; var end = ticks.last().startOffset
                            if (end - start >= 2 && source[start] == ' ' && source[end - 1] == ' ' && source.substring(start, end).isNotBlank()) { start++; end-- }
                            add(Span(a, start, end, b, MarkdownStyle.InlineCode))
                            for (i in start until end) {
                                literal[i] = true
                                if (source[i] == '\n' || source[i] == '\r') replacements[i] = Replacement(i, i + 1, " ")
                            }
                        }
                        return
                    }
                    E.CODE_FENCE -> {
                        val firstEol = node.children.firstOrNull { it.type == T.EOL }
                        val close = node.children.lastOrNull { it.type == T.CODE_FENCE_END }
                        if (firstEol != null && close != null) {
                            val start = firstEol.endOffset
                            val end = (close.startOffset - 1).coerceAtLeast(start)
                            val info = node.children.firstOrNull { it.type == T.FENCE_LANG }?.let { source.substring(it.startOffset, it.endOffset) }.orEmpty()
                            add(Span(a, start, end, b, MarkdownStyle.CodeBlock, codeInfo = info))
                            for (i in start until end) literal[i] = true
                        } else {
                            spans += Span(a, a, b, b, kind = Kind.RAW)
                            for (i in a until b) raw[i] = true
                        }
                        return
                    }
                    E.IMAGE, E.INLINE_LINK -> {
                        val link = if (node.type == E.IMAGE) node.children.firstOrNull { it.type == E.INLINE_LINK } else node
                        val label = link?.children?.firstOrNull { it.type == E.LINK_TEXT }
                        val destination = link?.children?.firstOrNull { it.type == E.LINK_DESTINATION }
                        if (label != null && destination != null) {
                            if (link.children.any { it.type == E.LINK_TITLE || it.type == T.LINK_TITLE }) {
                                spans += Span(a, a, b, b, kind = Kind.RAW)
                                for (i in a until b) raw[i] = true
                                return
                            }
                            val url = unescape(source.substring(destination.startOffset, destination.endOffset).removeSurrounding("<", ">"))
                            val kind = when {
                                node.type == E.IMAGE -> Kind.IMAGE
                                Regex("/api/(?:core/)?files/").containsMatchIn(url) -> Kind.ATTACHMENT
                                else -> Kind.LINK
                            }
                            val span = Span(a, label.startOffset + 1, label.endOffset - 1, b, destination = url, kind = kind)
                            if (kind == Kind.IMAGE && span.contentStart == span.contentEnd) {
                                spans += span
                                replacements[a] = Replacement(a, b, url.substringBefore('?').substringAfterLast('/').ifBlank { "Image" })
                            } else add(span)
                            if (span.isAtomic) return
                            label.children.forEach(::walk)
                            return
                        }
                    }
                    T.HTML_TAG -> {
                        val match = htmlTag.matchEntire(source.substring(a, b))
                        if (match != null) {
                            val tag = match.groupValues[1].lowercase()
                            if (!source.startsWith("</", a)) htmlStack += tag to (a until b)
                            else {
                                val at = htmlStack.indexOfLast { it.first == tag }
                                if (at >= 0) {
                                    val open = htmlStack.removeAt(at).second
                                    val style = htmlStyles.getValue(tag)
                                    add(Span(open.first, open.last + 1, a, b, style))
                                    if (style == MarkdownStyle.InlineCode) for (i in open.last + 1 until a) literal[i] = true
                                }
                            }
                        } else {
                            spans += Span(a, a, b, b, kind = Kind.RAW)
                            for (i in a until b) raw[i] = true
                        }
                        return
                    }
                    T.LIST_BULLET, T.LIST_NUMBER, T.BLOCK_QUOTE -> if (a < b) {
                        val style = when (node.type) { T.LIST_BULLET -> MarkdownStyle.UnorderedList; T.LIST_NUMBER -> MarkdownStyle.OrderedList; else -> MarkdownStyle.Blockquote }
                        val lineEnd = source.indexOf('\n', a).let { if (it < 0) source.length else it }
                        if (style == MarkdownStyle.Blockquote) {
                            val bodyStart = (b + if (source.getOrNull(b) == ' ') 1 else 0).coerceAtMost(lineEnd)
                            add(Span(a, bodyStart, lineEnd, lineEnd, style))
                        } else spans += Span(a, a, lineEnd, lineEnd, style)
                        if (node.type == T.LIST_BULLET) replacements[a] = Replacement(a, a + 1, "•")
                    }
                    E.HTML_BLOCK, E.CODE_BLOCK, E.FULL_REFERENCE_LINK, E.SHORT_REFERENCE_LINK, E.AUTOLINK, T.HORIZONTAL_RULE,
                    E.ATX_1, E.ATX_2, E.ATX_3, E.ATX_4, E.ATX_5, E.ATX_6, E.SETEXT_1, E.SETEXT_2,
                    GFMElementTypes.TABLE, GFMElementTypes.INLINE_MATH, GFMElementTypes.BLOCK_MATH -> {
                        spans += Span(a, a, b, b, kind = Kind.RAW)
                        for (i in a until b) raw[i] = true
                        return
                    }
                }
                node.children.forEach(::walk)
            }
            walk(tree)
            // CommonMark can omit a final empty item. Keep its native bullet and active
            // block state while the user is about to type the item's first character.
            var lineStart = 0
            for (line in source.split('\n')) {
                val marker = Regex("^(?:[-+*] |[0-9]+\\. |>[ ])$").matches(line)
                if (marker && (lineStart until lineStart + line.length).none { raw[it] || literal[it] || hidden[it] } &&
                    spans.none { it.style in setOf(MarkdownStyle.UnorderedList, MarkdownStyle.OrderedList, MarkdownStyle.Blockquote) && it.start == lineStart }) {
                    val style = when (line.first()) { '>' -> MarkdownStyle.Blockquote; '-', '+', '*' -> MarkdownStyle.UnorderedList; else -> MarkdownStyle.OrderedList }
                    if (style == MarkdownStyle.Blockquote) add(Span(lineStart, lineStart + line.length, lineStart + line.length, lineStart + line.length, style))
                    else spans += Span(lineStart, lineStart, lineStart + line.length, lineStart + line.length, style)
                    if (style == MarkdownStyle.UnorderedList) replacements[lineStart] = Replacement(lineStart, lineStart + 1, "•")
                }
                lineStart += line.length + 1
            }
            // Pending toolbar marks are represented by balanced empty HTML spans.
            // Accept legacy empty Markdown drafts as well, without treating their markers as content.
            if (source.matches(Regex("\\*{2}|\\*{4}|\\*{6}|``"))) {
                hidden.fill(true)
                val middle = source.length / 2
                for (i in 0 until middle) opening[i] = true
                when (source.length) {
                    2 -> spans += Span(0, 1, 1, 2, if (source == "``") MarkdownStyle.InlineCode else MarkdownStyle.Italic)
                    4 -> spans += Span(0, 2, 2, 4, MarkdownStyle.Bold)
                    6 -> { spans += Span(0, 2, 4, 6, MarkdownStyle.Bold); spans += Span(2, 3, 3, 4, MarkdownStyle.Italic) }
                }
            }
            escapedOrEntity.findAll(source).forEach { match ->
                if (match.range.none { hidden[it] || literal[it] || raw[it] } && atomicSpans.none { match.range.first in it.start until it.end }) {
                    val decoded = unescape(match.value)
                    if (decoded != match.value) replacements[match.range.first] = Replacement(match.range.first, match.range.last + 1, decoded)
                }
            }
            // HTML code is literal even when its contents resemble emphasis.
            spans.filter { it.style == MarkdownStyle.InlineCode && source.regionMatches(it.start, "<code>", 0, 6, ignoreCase = true) }.forEach { code ->
                for (i in code.contentStart until code.contentEnd) { hidden[i] = false; replacements.remove(i) }
                spans.removeAll { it !== code && it.start >= code.contentStart && it.end <= code.contentEnd }
                // HTML character references decode inside <code>; Markdown escapes stay literal.
                escapedOrEntity.findAll(source.substring(code.contentStart, code.contentEnd)).filter { it.value.startsWith('&') }.forEach { match ->
                    val start = code.contentStart + match.range.first
                    replacements[start] = Replacement(start, code.contentStart + match.range.last + 1, unescape(match.value))
                }
            }
            val toVisible = IntArray(source.length + 1)
            val toSource = mutableListOf(0)
            val visible = StringBuilder()
            var i = 0
            while (i < source.length) {
                toVisible[i] = visible.length
                val replacement = replacements[i]
                when {
                    replacement != null && !hidden[i] -> {
                        for (at in i until replacement.end) toVisible[at] = visible.length
                        replacement.text.forEachIndexed { n, char -> visible.append(char); toSource += if (n == replacement.text.lastIndex) replacement.end else i }
                        i = replacement.end
                    }
                    hidden[i] -> { if (opening[i]) toSource[visible.length] = i + 1; i++ }
                    else -> { visible.append(source[i]); toSource += i + 1; i++ }
                }
            }
            toVisible[source.length] = visible.length
            if (selection?.collapsed == true) {
                val cursor = selection.start.coerceIn(0, source.length)
                toSource[toVisible[cursor]] = cursor
            }
            val mapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = toVisible[offset.coerceIn(0, source.length)]
                override fun transformedToOriginal(offset: Int) = toSource[offset.coerceIn(0, visible.length)]
            }
            return MarkdownPresentation(source, visible.toString(), spans, mapping)
        }
    }
}
