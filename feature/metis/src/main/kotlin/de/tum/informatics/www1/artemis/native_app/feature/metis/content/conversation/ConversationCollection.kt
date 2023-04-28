package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableTitle

data class ConversationCollection(
    val favorites: List<Conversation>,
    val channels: List<ChannelChat>,
    val groupChats: List<GroupChat>,
    val directChats: List<OneToOneChat>,
    val hidden: List<Conversation>
) {
    fun filtered(query: String): ConversationCollection {
        return ConversationCollection(
            channels = channels.filter { it.filterPredicate(query) },
            groupChats = groupChats.filter { it.filterPredicate(query) },
            directChats = directChats.filter { it.filterPredicate(query) },
            favorites = favorites.filter { it.filterPredicate(query) },
            hidden = hidden.filter { it.filterPredicate(query) }
        )
    }

}

private fun Conversation.filterPredicate(query: String): Boolean = when (this) {
    is ChannelChat -> query in name
    is GroupChat -> query in humanReadableTitle
    is OneToOneChat -> query in humanReadableTitle
}
