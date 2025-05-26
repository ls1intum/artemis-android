package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat

object ConversationCollectionInitUtil {

    fun fromConversationList(
        conversations: List<Conversation>,
        preferences: ConversationPreferenceService.Preferences,
        filterActive: Boolean = false
    ): ConversationCollections {
        return ConversationCollections(
            collections = listOf(
                ConversationCollection(
                    section = ConversationsOverviewSection.FAVOURITES,
                    conversations = conversations.filter { it.isFavorite },
                    isExpanded = preferences.isExpanded(ConversationsOverviewSection.FAVOURITES),
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.CHANNELS,
                    conversations = conversations.filterNotHidden<ChannelChat>()
                        .filter {
                            !it.filterPredicate("exercise") && !it.filterPredicate("lecture") && !it.filterPredicate("exam")
                        },
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.CHANNELS),
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.EXERCISES,
                    conversations = conversations.filter {
                        it is ChannelChat && !it.isHidden && it.filterPredicate("exercise")
                    }.map { it as ChannelChat },
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.EXERCISES),
                    showPrefix = false
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.LECTURES,
                    conversations = conversations.filter {
                        it is ChannelChat && !it.isHidden && it.filterPredicate("lecture")
                    }.map { it as ChannelChat },
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.LECTURES),
                    showPrefix = false
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.EXAMS,
                    conversations = conversations.filter {
                        it is ChannelChat && !it.isHidden && it.filterPredicate("exam")
                    }.map { it as ChannelChat },
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.EXAMS),
                    showPrefix = false
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.GROUP_CHATS,
                    conversations = conversations.filterNotHidden<GroupChat>(),
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.GROUP_CHATS)
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.DIRECT_MESSAGES,
                    conversations = conversations.filterNotHidden<OneToOneChat>(),
                    isExpanded = filterActive || preferences.isExpanded(ConversationsOverviewSection.DIRECT_MESSAGES)
                ),
                ConversationCollection(
                    section = ConversationsOverviewSection.HIDDEN,
                    conversations = conversations.filter { it.isHidden },
                    isExpanded = preferences.isExpanded(ConversationsOverviewSection.HIDDEN),
                ),
            )
        )
    }

    private inline fun <reified T : Conversation> List<*>.filterNotHidden(): List<T> {
        return filterIsInstance<T>()
            .filter { !it.isHidden }
    }
}