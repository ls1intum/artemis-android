package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ConversationConfiguration(
    /**
     * Used to determine the animation when switching between configurations.
     */
    val navigationLevel: Int,
    val prevConfiguration: ConversationConfiguration?
) : Parcelable

@Parcelize
data object NothingOpened : ConversationConfiguration(0, null)

@Parcelize
data class OpenedConversation(
    private val _prevConfiguration: ConversationConfiguration,
    val conversationId: Long,
    val openedThread: OpenedThread?
) :
    ConversationConfiguration(10, _prevConfiguration)

@Parcelize
data class OpenedThread(val conversationId: Long, val postId: StandalonePostId) : Parcelable

@Parcelize
data class OpenedSavedPosts(
    private val _prevConfiguration: ConversationConfiguration,
    val status: SavedPostStatus
)
    : ConversationConfiguration(5, _prevConfiguration)

/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified username.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
data class NavigateToUserConversation(
    private val _prevConfiguration: ConversationConfiguration,
    val username: String
) : ConversationConfiguration(0, _prevConfiguration)

@Parcelize
internal data class AddChannelConfiguration(
    private val _prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration(10, _prevConfiguration)

@Parcelize
internal data class BrowseChannelConfiguration(
    private val _prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration(10, _prevConfiguration)

@Parcelize
internal data class CreatePersonalConversation(
    private val _prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration(10, _prevConfiguration)

@Parcelize
internal data class ConversationSettings(
    val conversationId: Long,
    private val _prevConfiguration: ConversationConfiguration,
    val isAddingMembers: Boolean = false,
    val isViewingAllMembers: Boolean = false
) : ConversationConfiguration(
    navigationLevel = _prevConfiguration.navigationLevel + 1,
    prevConfiguration = _prevConfiguration
)