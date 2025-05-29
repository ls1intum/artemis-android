package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation

data class ConversationCollections(
    val collections: List<ConversationCollection<out Conversation>>,
) {
    val conversations: List<Conversation>
        get() = collections.flatMap { it.conversations }

    fun filtered(query: String): ConversationCollections = filterBy { it.filterPredicate(query) }

    fun filterUnread(): ConversationCollections = filterBy { (it.unreadMessagesCount ?: 0) > 0 }

    fun filterRecent(recentChannels: List<Conversation>): ConversationCollections {
        val recentConversations = recentChannels.toSet()
        return filterBy { it in recentConversations }
    }

    fun filterUnresolved(unresolvedChannels: List<Conversation>): ConversationCollections {
        val unresolved = unresolvedChannels.toSet()
        return filterBy { unresolved.any { unresolved -> unresolved.id == it.id }  }
    }

    fun hasUnreadMessages(): Boolean = conversations.any { (it.unreadMessagesCount ?: 0) > 0 }

    private fun filterBy(predicate: (Conversation) -> Boolean): ConversationCollections {
        return ConversationCollections(
            collections = collections.map { collection ->
                collection.filter(predicate)
            }
        )
    }

    data class ConversationCollection<T : Conversation>(
        val section: ConversationsOverviewSection,
        val conversations: List<T>,
        val isExpanded: Boolean,
        val showPrefix: Boolean = true
    ) {
        fun filter(predicate: (Conversation) -> Boolean) =
            copy(conversations = conversations.filter(predicate))
    }
}