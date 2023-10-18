package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import android.text.style.ForegroundColorSpan
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import org.commonmark.parser.Parser

class UserTagPlugin : AbstractMarkwonPlugin() {

    override fun configureParser(builder: Parser.Builder) {
        builder.customBlockParserFactory(UserTagBlockParserFactory())
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(UserTagBlock::class.java) { visitor, simpleExtNode ->
            val length = visitor.length()

            visitor.builder().append("@")
            visitor.builder().append(simpleExtNode.username)

            SpannableBuilder.setSpans(
                visitor.builder(),
                arrayOf(ForegroundColorSpan(0xff3e8acc.toInt())),
                length,
                visitor.length()
            )
        }
    }
}
