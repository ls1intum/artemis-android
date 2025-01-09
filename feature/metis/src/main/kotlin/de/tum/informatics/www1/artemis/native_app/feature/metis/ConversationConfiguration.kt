package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ConversationConfiguration(
    /**
     * Used to determine the animation when switching between configurations.
     */
    val navigationLevel: Int
) : Parcelable

@Parcelize
data object NothingOpened : ConversationConfiguration(0)

@Parcelize
data class OpenedConversation(val conversationId: Long, val openedThread: OpenedThread?) :
    ConversationConfiguration(1)

@Parcelize
data class OpenedThread(val conversationId: Long, val postId: StandalonePostId) : Parcelable

/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified username.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
sealed class NavigateToUserConversation : ConversationConfiguration(0)

@Parcelize
data class NavigateToUserConversationByUsername(val username: String) : NavigateToUserConversation()

@Parcelize data class NavigateToUserConversationById(val userId: Long) : NavigateToUserConversation()

@Parcelize
internal data class AddChannelConfiguration(
    val prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration(1)

@Parcelize
internal data class BrowseChannelConfiguration(
    val prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration(1)

@Parcelize
internal data class CreatePersonalConversation(val prevConfiguration: ConversationConfiguration) :
    ConversationConfiguration(1)

@Parcelize
internal data class ConversationSettings(
    val conversationId: Long,
    val prevConfiguration: ConversationConfiguration,
    val isAddingMembers: Boolean = false,
    val isViewingAllMembers: Boolean = false
) : ConversationConfiguration(
    navigationLevel = when(prevConfiguration) {
        is ConversationSettings -> 3
        else -> 2
    }
)