package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.humanReadableName

data class ConversationCollections(
    val favorites: ConversationCollection<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>,
    val channels: ConversationCollection<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat>,
    val groupChats: ConversationCollection<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat>,
    val directChats: ConversationCollection<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat>,
    val hidden: ConversationCollection<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>
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

    data class ConversationCollection<T : de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>(
        val conversations: List<T>,
        val isExpanded: Boolean
    ) {
        fun filter(predicate: (de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation) -> Boolean) =
            copy(conversations = conversations.filter(predicate))
    }
}

private fun de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation.filterPredicate(query: String): Boolean =
    humanReadableName.contains(query, ignoreCase = true)
