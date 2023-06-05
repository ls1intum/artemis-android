package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableTitle

data class ConversationCollections(
    val favorites: ConversationCollection<Conversation>,
    val channels: ConversationCollection<ChannelChat>,
    val groupChats: ConversationCollection<GroupChat>,
    val directChats: ConversationCollection<OneToOneChat>,
    val hidden: ConversationCollection<Conversation>
) {
    fun filtered(query: String): ConversationCollections {
        return ConversationCollections(
            channels = channels.filter { it.filterPredicate(query) },
            groupChats = groupChats.filter { it.filterPredicate(query) },
            directChats = directChats.filter { it.filterPredicate(query) },
            favorites = favorites.filter { it.filterPredicate(query) },
            hidden = hidden.filter { it.filterPredicate(query) }
        )
    }

    data class ConversationCollection<T : Conversation>(val conversations: List<T>, val isExpanded: Boolean) {
        fun filter(predicate: (Conversation) -> Boolean) = copy(conversations = conversations.filter(predicate))
    }
}

private fun Conversation.filterPredicate(query: String): Boolean = when (this) {
    is ChannelChat -> query in name
    is GroupChat -> query in humanReadableTitle
    is OneToOneChat -> query in humanReadableTitle
}
