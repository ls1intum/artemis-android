package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat

data class ConversationCollections(
    val favorites: ConversationCollection<Conversation>,
    val channels: ConversationCollection<ChannelChat>,
    val groupChats: ConversationCollection<GroupChat>,
    val directChats: ConversationCollection<OneToOneChat>,
    val hidden: ConversationCollection<Conversation>,
    val exerciseChannels: ConversationCollection<ChannelChat>,
    val lectureChannels: ConversationCollection<ChannelChat>,
    val examChannels: ConversationCollection<ChannelChat>,
    val recentChannels: ConversationCollection<Conversation>
) {
    val conversations: List<Conversation>
        get() = favorites.conversations +
                channels.conversations +
                groupChats.conversations +
                directChats.conversations +
                hidden.conversations +
                exerciseChannels.conversations +
                lectureChannels.conversations +
                examChannels.conversations +
                recentChannels.conversations

    fun filtered(query: String): ConversationCollections {
        return ConversationCollections(
            channels = channels.filter { it.filterPredicate(query) },
            groupChats = groupChats.filter { it.filterPredicate(query) },
            directChats = directChats.filter { it.filterPredicate(query) },
            favorites = favorites.filter { it.filterPredicate(query) },
            hidden = hidden.filter { it.filterPredicate(query) },
            exerciseChannels = exerciseChannels.filter { it.filterPredicate(query) },
            lectureChannels = lectureChannels.filter { it.filterPredicate(query) },
            examChannels = examChannels.filter { it.filterPredicate(query) },
            recentChannels = recentChannels.filter { it.filterPredicate(query) }
        )
    }

    fun filterUnread() : ConversationCollections {
        return ConversationCollections(
            channels = channels.filter { (it.unreadMessagesCount ?: 0) > 0 },
            groupChats = groupChats.filter { (it.unreadMessagesCount ?: 0) > 0 },
            directChats = directChats.filter { (it.unreadMessagesCount ?: 0) > 0 },
            favorites = favorites.filter { (it.unreadMessagesCount ?: 0) > 0 },
            hidden = hidden.filter { (it.unreadMessagesCount ?: 0) > 0 },
            exerciseChannels = exerciseChannels.filter { (it.unreadMessagesCount ?: 0) > 0 },
            lectureChannels = lectureChannels.filter { (it.unreadMessagesCount ?: 0) > 0 },
            examChannels = examChannels.filter { (it.unreadMessagesCount ?: 0) > 0 },
            recentChannels = recentChannels.filter { (it.unreadMessagesCount ?: 0) > 0 }
        )
    }

    data class ConversationCollection<T : Conversation>(
        val conversations: List<T>,
        val isExpanded: Boolean,
        val showPrefix: Boolean = true
    ) {
        fun filter(predicate: (Conversation) -> Boolean) =
            copy(conversations = conversations.filter(predicate))
    }
}