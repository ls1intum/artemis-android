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
    val examChannels: ConversationCollection<ChannelChat>
) {
    val conversations: List<Conversation>
        get() = favorites.conversations +
                channels.conversations +
                groupChats.conversations +
                directChats.conversations +
                hidden.conversations +
                exerciseChannels.conversations +
                lectureChannels.conversations +
                examChannels.conversations

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

    private fun filterBy(predicate: (Conversation) -> Boolean): ConversationCollections {
        return ConversationCollections(
            channels = channels.filter(predicate),
            groupChats = groupChats.filter(predicate),
            directChats = directChats.filter(predicate),
            favorites = favorites.filter(predicate),
            hidden = hidden.filter(predicate),
            exerciseChannels = exerciseChannels.filter(predicate),
            lectureChannels = lectureChannels.filter(predicate),
            examChannels = examChannels.filter(predicate)
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