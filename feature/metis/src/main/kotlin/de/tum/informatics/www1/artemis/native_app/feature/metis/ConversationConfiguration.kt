package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.UserIdentifier
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
data class OpenedThread(val postId: StandalonePostId) : Parcelable

data class OpenedSavedPosts(
    private val _prevConfiguration: ConversationConfiguration,
    val status: SavedPostStatus
)
    : ConversationConfiguration(5, _prevConfiguration)
/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified identifier.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
data class NavigateToUserConversation(
    private val _prevConfiguration: ConversationConfiguration,
    val userIdentifier: UserIdentifier,
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