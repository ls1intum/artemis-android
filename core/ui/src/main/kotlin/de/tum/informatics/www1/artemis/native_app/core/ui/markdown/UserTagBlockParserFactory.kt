package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import org.commonmark.node.Block
import org.commonmark.parser.InlineParser
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState

class UserTagBlockParserFactory : AbstractBlockParserFactory() {

    companion object {
        private val userMarkdownPattern = "\\[user](.*?)\\((.*?)\\)\\[/user]".toRegex()
    }

    override fun tryStart(
        state: ParserState,
        matchedBlockParser: MatchedBlockParser
    ): BlockStart? {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line

        val relevantLine = line.subSequence(nextNonSpace, line.length)
        return if (relevantLine.startsWith("[user]") && relevantLine.contains("[/user]")) {
            BlockStart.of(UserTagBlockParser()).atIndex(state.index)
        } else {
            null
        }
    }

    private class UserTagBlockParser : AbstractBlockParser() {

        private var done = false

        private val block = UserTagBlock()
        private var content: String = ""

        override fun getBlock(): Block = block

        override fun isContainer(): Boolean = false

        override fun tryContinue(parserState: ParserState): BlockContinue = BlockContinue.none()

        override fun addLine(line: CharSequence) {
            if (line.contains("[/user]")) done = true
            content += line.toString()
        }

        override fun closeBlock() {
            userMarkdownPattern.find(content)?.let { match ->
                block.username = match.groups[1]?.value.orEmpty()
            }
        }

        override fun parseInlines(inlineParser: InlineParser?) {
            super.parseInlines(inlineParser)
        }
    }
}
