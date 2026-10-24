package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

/** The native editor sees visible text. Only this boundary translates edits into Markdown. */
internal object MarkdownEditEngine {
    private val paragraphStyles = setOf(MarkdownStyle.UnorderedList, MarkdownStyle.OrderedList, MarkdownStyle.Blockquote)
    fun insertText(current: TextFieldValue, text: String, typingMarks: Set<MarkdownStyle>? = null): TextFieldValue {
        val visible = visibleValue(current)
        return applyVisibleEdit(current, TextFieldValue(visible.text.replaceRange(visible.selection.min, visible.selection.max, text), TextRange(visible.selection.min + text.length)), typingMarks)
    }

    fun insertLink(current: TextFieldValue, label: String, destination: String, preserveMarkup: Boolean): TextFieldValue? {
        val p = MarkdownPresentation.parse(current.text)
        val range = snapAtoms(p, visibleValue(current).selection)
        if (p.spans.any { it.isAtomic && p.visibleRange(it).let { r -> range.min < r.max && range.max > r.min } }) return null
        val inherited = marksAt(p, current.selection.min).intersect(MarkdownDocument.inlineStyles + paragraphStyles)
        val labelGlyphs = if (preserveMarkup) MarkdownDocument.glyphs(MarkdownPresentation.parse(label)) else label.map { MarkdownGlyph(it) }
        val inserted = labelGlyphs.map { it.copy(marks = it.marks + inherited, link = destination) }
        val result = replace(p, range.min, range.max, inserted, TextRange(range.min + inserted.size), null, inherited)
        val next = MarkdownPresentation.parse(result.text)
        val link = next.spans.lastOrNull { it.kind == MarkdownPresentation.Kind.LINK && it.destination == destination && next.visibleRange(it).max == range.min + inserted.size }
        return if (link == null) result else result.copy(selection = TextRange(link.end))
    }
    fun visibleValue(value: TextFieldValue): TextFieldValue {
        val p = MarkdownPresentation.parse(value.text, value.selection)
        fun map(range: TextRange) = TextRange(p.offsetMapping.originalToTransformed(range.start), p.offsetMapping.originalToTransformed(range.end))
        return TextFieldValue(p.text, map(value.selection), value.composition?.let(::map))
    }
    private fun marksAt(p: MarkdownPresentation, position: Int) = p.spans.filter {
        it.style != null && position >= (if (it.style == MarkdownStyle.Blockquote) it.start else it.contentStart) && position <= it.contentEnd &&
            (position < it.end || it.style in setOf(MarkdownStyle.UnorderedList, MarkdownStyle.OrderedList, MarkdownStyle.Blockquote))
    }.mapNotNull { it.style }.toSet()

    fun activeMarks(current: TextFieldValue): Set<MarkdownStyle> {
        val p = MarkdownPresentation.parse(current.text)
        if (current.selection.collapsed) return marksAt(p, current.selection.start)
        val from = p.offsetMapping.originalToTransformed(current.selection.min)
        val to = p.offsetMapping.originalToTransformed(current.selection.max)
        val selected = MarkdownDocument.glyphs(p).subList(from, to).filterNot { it.char.isWhitespace() }
        return selected.map { it.marks }.reduceOrNull { a, b -> a.intersect(b) }.orEmpty()
    }

    fun applyVisibleEdit(current: TextFieldValue, incoming: TextFieldValue, typingMarks: Set<MarkdownStyle>? = null): TextFieldValue {
        val p = MarkdownPresentation.parse(current.text)
        val old = visibleValue(current)
        if (incoming.text == old.text) return selection(current, incoming.selection, incoming.composition)
        var from = old.text.commonPrefixWith(incoming.text).length
        var suffix = old.text.substring(from).commonSuffixWith(incoming.text.substring(from)).length
        // Preserve the user's intended replacement range when repeated characters make a minimal diff ambiguous.
        val selected = old.selection
        if (!selected.collapsed && incoming.text.startsWith(old.text.substring(0, selected.min)) && incoming.text.endsWith(old.text.substring(selected.max))) {
            from = selected.min
            suffix = old.text.length - selected.max
        }
        var to = old.text.length - suffix
        var inserted = incoming.text.substring(from, incoming.text.length - suffix)
        val insertLength = incoming.text.length - (old.text.length - (old.selection.max - old.selection.min))
        if (insertLength >= 0 && incoming.text.take(old.selection.min) == old.text.take(old.selection.min) &&
            incoming.text.substring((old.selection.min + insertLength).coerceAtMost(incoming.text.length)) == old.text.substring(old.selection.max)) {
            from = old.selection.min; to = old.selection.max
            inserted = incoming.text.substring(from, from + insertLength)
        } else if (old.selection.collapsed && incoming.selection.collapsed && incoming.text.length < old.text.length) {
            val removed = old.text.length - incoming.text.length
            val caret = old.selection.start
            when {
                incoming.selection.start == caret - removed && caret >= removed && old.text.removeRange(caret - removed, caret) == incoming.text -> {
                    from = caret - removed; to = caret; inserted = ""
                }
                incoming.selection.start == caret && caret + removed <= old.text.length && old.text.removeRange(caret, caret + removed) == incoming.text -> {
                    from = caret; to = caret + removed; inserted = ""
                }
            }
        }
        if (incoming.composition == null && inserted.isEmpty() && old.selection.collapsed && to == old.selection.start && from == to - 1) {
            removeBlockAtStart(current)?.let { return it }
        }
        var inherited = marksAt(p, current.selection.min)
        // A block prefix governs Enter and typing at its line's trailing caret too.
        inherited = inherited + p.spans.filter { it.style in paragraphStyles && current.selection.min in it.start..it.contentEnd }.mapNotNull { it.style }
        val containingCode = p.spans.firstOrNull { it.style == MarkdownStyle.CodeBlock && p.visibleRange(it).let { r -> from in r.min..r.max } }
        if (containingCode != null) inherited = inherited + MarkdownStyle.CodeBlock
        if (typingMarks != null) inherited = (inherited - MarkdownDocument.inlineStyles) + typingMarks
        var target = incoming.selection
        var renumber = false
        if (incoming.composition == null && MarkdownStyle.InlineCode !in inherited) {
            if (inserted == "\n" && from == to && containingCode != null) {
                exitCodeOnEmptyFinalParagraph(p, containingCode, from)?.let { return it }
            }
            val block = p.spans.firstOrNull { it.style in paragraphStyles && current.selection.min in it.start..it.contentEnd }
            if (block != null && MarkdownStyle.CodeBlock !in inherited && inserted.contains('\n')) {
                val bodyStart = blockBodyStart(p, block)
                val body = p.text.substring(p.offsetMapping.originalToTransformed(bodyStart), p.visibleRange(block).max)
                if (inserted == "\n" && body.isBlank()) {
                    val unblocked = removeBlockAtStart(current.copy(selection = TextRange(bodyStart)))
                    if (unblocked != null) {
                        val at = unblocked.selection.start
                        return TextFieldValue(unblocked.text.removeRange(at, at + block.contentEnd - bodyStart), TextRange(at))
                    }
                }
                if (block.style != MarkdownStyle.Blockquote) {
                    val prefix = p.source.substring(block.start, bodyStart).replaceFirst(Regex("^[-+*]"), "•")
                    var number = Regex("^(\\d+)\\. ").find(prefix)?.groupValues?.get(1)?.toLongOrNull()
                    val additions = mutableListOf<Pair<Int, String>>()
                    inserted.forEachIndexed { index, char ->
                        if (char == '\n') additions += index + 1 to if (number == null) prefix else "${++number}. "
                    }
                    fun map(offset: Int) = offset + additions.filter { from + it.first <= offset }.sumOf { it.second.length }
                    target = TextRange(map(target.start), map(target.end))
                    for ((at, nextPrefix) in additions.asReversed()) inserted = inserted.replaceRange(at, at, nextPrefix)
                    renumber = block.style == MarkdownStyle.OrderedList
                }
            }
        }
        val originalRange = TextRange(from, to)
        val snapped = snapAtoms(p, originalRange)
        from = snapped.min; to = snapped.max
        val sourceStart = p.offsetMapping.transformedToOriginal(from)
        val link = p.spans.firstOrNull { it.kind == MarkdownPresentation.Kind.LINK && current.selection.min in it.contentStart..it.contentEnd }?.destination
        val raw = p.spans.any { it.kind == MarkdownPresentation.Kind.RAW && sourceStart in it.start until it.end }
        val codeInfo = p.spans.firstOrNull { it.style == MarkdownStyle.CodeBlock && sourceStart in it.contentStart..it.contentEnd }?.codeInfo.orEmpty()
        val pieces = inserted.map { MarkdownGlyph(it, inherited, link, codeInfo = codeInfo, verbatim = raw) }
        val result = replace(p, from, to, pieces, if (snapped == originalRange) target else TextRange(from + inserted.length), incoming.composition, inherited,
            if (from == to) current.selection.start else null)
        return if (renumber) renumberOrderedParagraphs(result) else result
    }

    fun insertMarkup(current: TextFieldValue, markup: String, typingMarks: Set<MarkdownStyle>? = null): TextFieldValue {
        val p = MarkdownPresentation.parse(current.text)
        val range = snapAtoms(p, visibleValue(current).selection)
        val marks = marksAt(p, current.selection.min).let { inherited ->
            if (typingMarks == null) inherited else (inherited - MarkdownDocument.inlineStyles) + typingMarks
        }
        val pasted = MarkdownPresentation.parse(markup)
        val inlinePaste = pasted.spans.none { it.style in paragraphStyles || it.style == MarkdownStyle.CodeBlock || it.kind == MarkdownPresentation.Kind.RAW }
        val block = if (inlinePaste) p.spans.firstOrNull { it.style in paragraphStyles && current.selection.min in it.start..it.contentEnd } else null
        val inherited = marks.intersect(MarkdownDocument.inlineStyles - MarkdownStyle.InlineCode) + listOfNotNull(block?.style)
        val inserted = mutableListOf<MarkdownGlyph>()
        var line = 0L
        for (glyph in MarkdownDocument.glyphs(pasted)) {
            inserted += glyph.copy(marks = glyph.marks + inherited)
            if (glyph.char == '\n' && block != null && block.style != MarkdownStyle.Blockquote) {
                val prefix = p.source.substring(block.start, blockBodyStart(p, block)).replaceFirst(Regex("^[-+*]"), "•")
                val number = Regex("^(\\d+)\\. ").find(prefix)?.groupValues?.get(1)?.toLongOrNull()
                val nextPrefix = if (number == null) prefix else "${number + ++line}. "
                inserted += nextPrefix.map { MarkdownGlyph(it, setOfNotNull(block.style)) }
            }
        }
        val result = replace(p, range.min, range.max, inserted, TextRange(range.min + inserted.size), null, marks)
        return if (block?.style == MarkdownStyle.OrderedList && markup.contains('\n')) renumberOrderedParagraphs(result) else result
    }

    /** Removing a structural prefix keeps native caret positions and never inserts a display marker. */
    fun removeBlockAtStart(current: TextFieldValue): TextFieldValue? {
        if (!current.selection.collapsed || current.composition != null) return null
        val p = MarkdownPresentation.parse(current.text)
        val caret = p.offsetMapping.originalToTransformed(current.selection.start)
        val block = p.spans.firstOrNull { it.style in paragraphStyles &&
            caret == p.offsetMapping.originalToTransformed(blockBodyStart(p, it)) &&
            current.selection.start in it.start..it.end } ?: return null
        val bodyStart = blockBodyStart(p, block)
        val separate = p.spans.any { it.style in paragraphStyles && it.end == block.start - 1 }
        val replacement = if (separate) "\n" else ""
        val source = current.text.replaceRange(block.start, bodyStart, replacement)
        return TextFieldValue(source, TextRange(maxOf(current.selection.start, bodyStart) - (bodyStart - block.start) + replacement.length))
    }

    private fun blockBodyStart(p: MarkdownPresentation, block: MarkdownPresentation.Span): Int =
        if (block.style == MarkdownStyle.Blockquote) block.contentStart else block.start +
            Regex("^(?:[-+*] |\\d+[.)] )").find(p.source.substring(block.start, block.end))?.value.orEmpty().length

    private fun exitCodeOnEmptyFinalParagraph(p: MarkdownPresentation, code: MarkdownPresentation.Span, caret: Int): TextFieldValue? {
        val range = p.visibleRange(code)
        if (caret != range.max) return null
        val body = p.source.substring(code.contentStart, code.contentEnd)
        val lastLine = body.substringAfterLast('\n')
        if (lastLine.isNotBlank()) return null
        val remaining = if ('\n' in body) body.substringBeforeLast('\n') else ""
        val fenced = p.source.substring(0, code.contentStart) + remaining + p.source.substring(code.contentEnd, code.end)
        val following = p.source.substring(code.end)
        val separator = if (following.startsWith('\n')) "\n" else "\n\n"
        return TextFieldValue(fenced + separator + following, TextRange(fenced.length + separator.length))
    }

    private fun renumberOrderedParagraphs(value: TextFieldValue): TextFieldValue {
        val p = MarkdownPresentation.parse(value.text)
        val items = p.spans.filter { it.style == MarkdownStyle.OrderedList }.distinctBy { it.start }.sortedBy { it.start }
        val groups = mutableListOf<MutableList<MarkdownPresentation.Span>>()
        for (item in items) {
            val previous = groups.lastOrNull()?.lastOrNull()
            if (previous != null && value.text.substring(previous.end, item.start) == "\n") groups.last() += item
            else groups += mutableListOf(item)
        }
        val group = groups.firstOrNull { value.selection.start in it.first().start..it.last().end } ?: return value
        val changes = mutableListOf<Triple<Int, Int, String>>()
        var number: Long? = null
        for (item in group) {
            val digits = Regex("^\\d+").find(value.text.substring(item.start, item.end))?.value ?: continue
            number = number?.plus(1) ?: digits.toLongOrNull() ?: continue
            if (digits != number.toString()) changes += Triple(item.start, digits.length, number.toString())
        }
        var source = value.text
        for ((at, length, replacement) in changes.asReversed()) source = source.replaceRange(at, at + length, replacement)
        fun map(offset: Int) = offset + changes.filter { it.first < offset }.sumOf { (_, length, replacement) -> replacement.length - length }
        return TextFieldValue(source, TextRange(map(value.selection.start), map(value.selection.end)))
    }

    fun toggleInline(style: MarkdownStyle, current: TextFieldValue): TextFieldValue {
        val p = MarkdownPresentation.parse(current.text)
        val visible = visibleValue(current)
        if (visible.selection.collapsed) {
            val span = p.spans.lastOrNull { it.style == style && current.selection.start in it.contentStart..it.contentEnd && current.selection.start < it.end }
            if (span != null) {
                if (p.visibleRange(span).collapsed) {
                    val result = current.text.removeRange(span.contentEnd, span.end).removeRange(span.start, span.contentStart)
                    return TextFieldValue(result, TextRange((current.selection.start - (span.contentStart - span.start)).coerceIn(span.start, result.length)))
                }
                if (current.selection.start == span.contentEnd) return current.copy(selection = TextRange(span.end), composition = null)
                if (current.selection.start == span.contentStart) return current.copy(selection = TextRange(span.start), composition = null)
                return InlineSelectionFormatting.remove(style, current, span)
            }
            val tag = when (style) { MarkdownStyle.Bold -> "strong"; MarkdownStyle.Italic -> "em"; MarkdownStyle.Underline -> "ins"; MarkdownStyle.Strikethrough -> "s"; else -> "code" }
            val at = current.selection.start
            return TextFieldValue(current.text.replaceRange(at, at, "<$tag></$tag>"), TextRange(at + tag.length + 2))
        }
        val range = snapAtoms(p, visible.selection)
        val glyphs = MarkdownDocument.glyphs(p)
        val selected = glyphs.subList(range.min, range.max)
        if (style == MarkdownStyle.InlineCode && selected.any { it.atom != null }) return current
        val remove = selected.filterNot { it.char.isWhitespace() }.all { style in it.marks }
        val changed = selected.map { it.copy(marks = if (remove) it.marks - style else it.marks + style) }
        return replace(p, range.min, range.max, changed, if (visible.selection.reversed) TextRange(range.max, range.min) else range, null, emptySet())
    }

    fun toggleCodeBlock(current: TextFieldValue): TextFieldValue {
        val p = MarkdownPresentation.parse(current.text)
        var range = snapAtoms(p, visibleValue(current).selection)
        val code = p.spans.firstOrNull { it.style == MarkdownStyle.CodeBlock && current.selection.min in it.contentStart..it.contentEnd }
        if (code != null) range = p.visibleRange(code)
        else {
                val start = p.text.lastIndexOf('\n', range.start - 1) + 1
                val end = p.text.indexOf('\n', range.end).let { if (it < 0) p.text.length else it }
                range = TextRange(start, end)
        }
        val glyphs = MarkdownDocument.glyphs(p)
        val selected = glyphs.subList(range.min, range.max)
        if (selected.any { it.atom != null }) return current
        if (selected.isEmpty()) {
            if (code != null) return TextFieldValue(current.text.removeRange(code.start, code.end), TextRange(code.start))
            val at = current.selection.start
            return TextFieldValue(current.text.replaceRange(at, at, "```\n\n```"), TextRange(at + 4))
        }
        val remove = selected.all { MarkdownStyle.CodeBlock in it.marks }
        val changed = selected.map { it.copy(marks = if (remove) it.marks - MarkdownStyle.CodeBlock else setOf(MarkdownStyle.CodeBlock), link = null) }
        return replace(p, range.min, range.max, changed, range, null, emptySet())
    }

    fun isActive(style: MarkdownStyle, current: TextFieldValue): Boolean {
        val p = MarkdownPresentation.parse(current.text)
        val range = visibleValue(current).selection
        if (range.collapsed) return style in marksAt(p, current.selection.start)
        val selected = MarkdownDocument.glyphs(p).subList(range.min, range.max).filterNot { it.char.isWhitespace() }
        return selected.isNotEmpty() && selected.all { style in it.marks }
    }

    private fun snapAtoms(p: MarkdownPresentation, range: TextRange): TextRange {
        var start = range.min; var end = range.max
        for (atom in p.spans.filter { it.isAtomic }) {
            val r = p.visibleRange(atom)
            if (range.collapsed && start > r.min && start < r.max) {
                start = if (start - r.min < r.max - start) r.min else r.max
                end = start
            } else if (!range.collapsed && start < r.max && end > r.min) {
                start = minOf(start, r.min); end = maxOf(end, r.max)
            }
        }
        return if (range.reversed) TextRange(end, start) else TextRange(start, end)
    }
    private fun selection(current: TextFieldValue, range: TextRange, composition: TextRange?): TextFieldValue {
        val p = MarkdownPresentation.parse(current.text, current.selection)
        val snapped = snapAtoms(p, range)
        fun raw(r: TextRange) = TextRange(p.offsetMapping.transformedToOriginal(r.start), p.offsetMapping.transformedToOriginal(r.end))
        return current.copy(selection = raw(snapped), composition = composition?.let(::raw))
    }
    private fun replace(p: MarkdownPresentation, from: Int, to: Int, inserted: List<MarkdownGlyph>, target: TextRange, composition: TextRange?, desiredMarks: Set<MarkdownStyle>, rawCaret: Int? = null): TextFieldValue {
        var start = rawCaret ?: p.offsetMapping.transformedToOriginal(from)
        var end = if (from == to && rawCaret != null) rawCaret else p.offsetMapping.transformedToOriginal(to)
        // Typing beside underscore emphasis changes its CommonMark flanking rules.
        // Include the immediate boundary once; subsequent expansion only includes overlaps.
        val adjacent = p.spans.filter { span -> span.style in setOf(MarkdownStyle.Bold, MarkdownStyle.Italic) &&
            p.source.getOrNull(span.start) == '_' && (span.end == start || span.start == end) }
        adjacent.forEach { start = minOf(start, it.start); end = maxOf(end, it.end) }
        // Include complete enclosing inline/code spans, so no edit can split transport syntax.
        repeat(24) {
            var expanded = false
            for (span in p.spans.filter { it.kind != MarkdownPresentation.Kind.RAW }) {
                if (span.start < end && span.end > start || start == end && start in (if (span.style in setOf(MarkdownStyle.CodeBlock, MarkdownStyle.Blockquote)) span.start..span.end else span.contentStart..span.contentEnd)) {
                    val a = minOf(start, span.start); val b = maxOf(end, span.end)
                    if (a != start || b != end) { start = a; end = b; expanded = true }
                }
            }
            if (!expanded) return@repeat
        }
        val a = p.offsetMapping.originalToTransformed(start)
        val b = p.offsetMapping.originalToTransformed(end)
        val glyphs = MarkdownDocument.glyphs(p)
        val changed = glyphs.subList(a, from) + inserted + glyphs.subList(to, b)
        var encoded = MarkdownDocument.encode(changed)
        if (changed.isEmpty() && MarkdownStyle.CodeBlock in desiredMarks) {
            val info = p.spans.firstOrNull { it.style == MarkdownStyle.CodeBlock && start in it.start..it.end }?.codeInfo.orEmpty()
            encoded = "```$info\n\n```"
        }
        // Preserve a pending style on a newly empty line without placing markers in native text.
        if (target.collapsed && target.start == a + changed.size && changed.lastOrNull()?.char == '\n') {
            if (MarkdownStyle.Blockquote in desiredMarks) encoded += "> "
            val pending = desiredMarks.intersect(MarkdownDocument.inlineStyles)
            val tags = listOf(MarkdownStyle.Bold to "strong", MarkdownStyle.Italic to "em", MarkdownStyle.Underline to "ins", MarkdownStyle.Strikethrough to "s", MarkdownStyle.InlineCode to "code").filter { it.first in pending }.map { it.second }
            encoded += tags.joinToString("") { "<$it>" } + tags.asReversed().joinToString("") { "</$it>" }
        }
        var source = p.source.replaceRange(start, end, encoded)
        var next = MarkdownPresentation.parse(source)
        fun rawPosition(visible: Int): Int {
            val offset = visible.coerceIn(0, next.text.length)
            val default = next.offsetMapping.transformedToOriginal(offset)
            if (!target.collapsed || desiredMarks.isEmpty() && inserted.isEmpty()) return default
            val candidates = (0..source.length).filter { next.offsetMapping.originalToTransformed(it) == offset }
            val caretMarks = MarkdownDocument.inlineStyles + paragraphStyles + MarkdownStyle.CodeBlock
            return candidates.firstOrNull { marksAt(next, it).intersect(caretMarks) == desiredMarks.intersect(caretMarks) } ?: default
        }
        var rawSelection = TextRange(rawPosition(target.start), rawPosition(target.end))
        val pending = desiredMarks.intersect(MarkdownDocument.inlineStyles)
        if (target.collapsed && pending.isNotEmpty() && marksAt(next, rawSelection.start).intersect(MarkdownDocument.inlineStyles) != pending) {
            val tags = listOf(MarkdownStyle.Bold to "strong", MarkdownStyle.Italic to "em", MarkdownStyle.Underline to "ins", MarkdownStyle.Strikethrough to "s", MarkdownStyle.InlineCode to "code").filter { it.first in pending }.map { it.second }
            val opening = tags.joinToString("") { "<$it>" }
            source = source.replaceRange(rawSelection.start, rawSelection.start, opening + tags.asReversed().joinToString("") { "</$it>" })
            rawSelection = TextRange(rawSelection.start + opening.length)
            next = MarkdownPresentation.parse(source)
        }
        return TextFieldValue(source, rawSelection, composition?.let { TextRange(next.offsetMapping.transformedToOriginal(it.start.coerceIn(0, next.text.length)), next.offsetMapping.transformedToOriginal(it.end.coerceIn(0, next.text.length))) })
    }
}
