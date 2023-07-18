package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.humanReadableName

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

    data class ConversationCollection<T : Conversation>(
        val conversations: List<T>,
        val isExpanded: Boolean
    ) {
        fun filter(predicate: (Conversation) -> Boolean) =
            copy(conversations = conversations.filter(predicate))
    }
}

private fun Conversation.filterPredicate(query: String): Boolean =
    humanReadableName.contains(query, ignoreCase = true)
