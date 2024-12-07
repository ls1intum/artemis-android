package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext


sealed interface VisibleMetisContext {
    val metisContext: MetisContext

    fun isInConversation(conversationId: Long): Boolean {
        return metisContext is MetisContext.Conversation &&
                (metisContext as MetisContext.Conversation).conversationId == conversationId
    }
}

data class VisiblePostList(override val metisContext: MetisContext) : VisibleMetisContext

data class VisibleStandalonePostDetails(override val metisContext: MetisContext, val postId: Long) :
    VisibleMetisContext
