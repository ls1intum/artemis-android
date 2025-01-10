package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
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
data class OpenedThread(val postId: StandalonePostId) : Parcelable

/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified username.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
sealed class NavigateToUserConversation(
    private val _prevConfiguration: ConversationConfiguration,
) : ConversationConfiguration(0, _prevConfiguration)

@Parcelize
data class NavigateToUserConversationByUsername(
    private val _prevConfiguration: ConversationConfiguration,
    val username: String
) : NavigateToUserConversation(_prevConfiguration)

@Parcelize
data class NavigateToUserConversationById(
    private val _prevConfiguration: ConversationConfiguration,
    val userId: Long
) : NavigateToUserConversation(_prevConfiguration)


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

/**
 * Technical configuration in which we want to ignore custom back handling and simply navigate back to the previous screen.
 *
 * This is useful eg for the back navigation after clicking on "send message" in the user profile dialog. This navigation
 * is implemented via a deeplink, so a new SinglePageConversationBody gets created. When pressing back, we want to navigate
 * back to the previous SinglePageConversationBody.
 */
@Parcelize
data object IgnoreCustomBackHandling : ConversationConfiguration(
    navigationLevel = 0,
    prevConfiguration = null
)