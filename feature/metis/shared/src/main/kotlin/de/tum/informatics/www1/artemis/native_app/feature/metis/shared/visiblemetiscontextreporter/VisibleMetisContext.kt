package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext


sealed interface VisibleMetisContext {
    val metisContext: MetisContext

    fun isInConversation(conversationId: Long): Boolean {
        return metisContext is MetisContext.Conversation &&
                (metisContext as MetisContext.Conversation).conversationId == conversationId
    }
}

data class VisibleCourse(override val metisContext: MetisContext.Course) : VisibleMetisContext

data class VisiblePostList(override val metisContext: MetisContext.Conversation) : VisibleMetisContext

data class VisibleStandalonePostDetails(override val metisContext: MetisContext.Conversation, val postId: Long) :
    VisibleMetisContext
