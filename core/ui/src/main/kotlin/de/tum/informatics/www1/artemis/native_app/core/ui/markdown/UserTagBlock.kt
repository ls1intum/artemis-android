package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import org.commonmark.node.CustomBlock
import org.commonmark.node.Visitor

class UserTagBlock : CustomBlock() {

    var username: String = ""

    override fun accept(visitor: Visitor) = visitor.visit(this)
}
