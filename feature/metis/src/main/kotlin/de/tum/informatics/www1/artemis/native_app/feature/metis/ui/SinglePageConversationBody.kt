package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsScreen
import kotlinx.parcelize.Parcelize

@Composable
internal fun SinglePageConversationBody(
    modifier: Modifier,
    courseId: Long,
    initialConfiguration: ConversationConfiguration = NothingOpened
) {
    var configuration: ConversationConfiguration by rememberSaveable(initialConfiguration) {
        mutableStateOf(initialConfiguration)
    }

    val openConversation = { conversationId: Long ->
        configuration = when (configuration) {
            is OpenedConversation -> OpenedConversation(conversationId, null)
            else -> OpenedConversation(conversationId, null)
        }
    }

    BackHandler(configuration != NothingOpened) {
        when (val config = configuration) {
            is ConversationSettings -> configuration = config.prevConfiguration
            is AddChannelConfiguration -> configuration = config.prevConfiguration
            is CreatePersonalConversation -> configuration = config.prevConfiguration
            is OpenedConversation -> configuration =
                if (config.openedThread != null) config.copy(openedThread = null) else NothingOpened

            is NavigateToUserConversation -> configuration = NothingOpened
            NothingOpened -> {}
        }
    }

    val ConversationOverview: @Composable (Modifier) -> Unit = { m ->
        ConversationOverviewBody(
            modifier = m.padding(top = 16.dp),
            courseId = courseId,
            onNavigateToConversation = openConversation,
            onRequestCreatePersonalConversation = {
                configuration = CreatePersonalConversation(configuration)
            },
            onRequestAddChannel = {
                configuration = AddChannelConfiguration(false, configuration)
            }
        )
    }

    when (val config = configuration) {
        NothingOpened -> {
            ConversationOverview(modifier)
        }

        is OpenedConversation -> {
            ConversationScreen(
                modifier = modifier,
                conversationId = config.conversationId,
                threadPostId = config.openedThread?.postId,
                courseId = courseId,
                onOpenThread = { postId ->
                    configuration = OpenedConversation(
                        config.conversationId,
                        OpenedThread(config.conversationId, postId)
                    )
                },
                onCloseThread = {
                    configuration = config.copy(openedThread = null)
                },
                onCloseConversation = {
                    configuration = NothingOpened
                },
                onNavigateToSettings = {
                    configuration = ConversationSettings(
                        conversationId = config.conversationId,
                        prevConfiguration = config
                    )
                },
                conversationsOverview = { mod -> ConversationOverview(mod) }
            )
        }

        is AddChannelConfiguration -> {
            if (config.isCreatingChannel) {
                CreateChannelScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onConversationCreated = openConversation,
                    onNavigateBack = {
                        configuration = AddChannelConfiguration(false, config.prevConfiguration)
                    }
                )
            } else {
                BrowseChannelsScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onNavigateToConversation = openConversation,
                    onNavigateToCreateChannel = {
                        configuration = AddChannelConfiguration(true, config.prevConfiguration)
                    },
                    onNavigateBack = { configuration = config.prevConfiguration }
                )
            }
        }

        is CreatePersonalConversation -> {
            CreatePersonalConversationScreen(
                modifier = modifier,
                courseId = courseId,
                onConversationCreated = openConversation,
                onNavigateBack = { configuration = config.prevConfiguration }
            )
        }

        is ConversationSettings -> {
            when {
                config.isViewingAllMembers -> {
                    ConversationMembersScreen(
                        modifier = modifier,
                        courseId = courseId,
                        conversationId = config.conversationId,
                        onNavigateBack = { configuration = config.prevConfiguration }
                    )
                }

                config.isAddingMembers -> {
                    ConversationAddMembersScreen(
                        modifier = modifier,
                        courseId = courseId,
                        onNavigateBack = { configuration = config.prevConfiguration }
                    )
                }

                else -> {
                    ConversationSettingsScreen(
                        modifier = modifier,
                        courseId = courseId,
                        conversationId = config.conversationId,
                        onNavigateBack = { configuration = config.prevConfiguration },
                        onRequestAddMembers = {
                            configuration = config.copy(
                                isAddingMembers = true,
                                prevConfiguration = configuration
                            )
                        },
                        onRequestViewAllMembers = {
                            configuration = config.copy(
                                isViewingAllMembers = true,
                                prevConfiguration = configuration
                            )
                        },
                        onConversationLeft = {
                            configuration = NothingOpened
                        }
                    )
                }
            }
        }

        is NavigateToUserConversation -> {
            NavigateToUserConversationUi(
                modifier = modifier,
                courseId = courseId,
                username = config.username,
                onNavigateToConversation = { conversationId ->
                    configuration = OpenedConversation(conversationId, null)
                },
                onNavigateBack = { configuration = NothingOpened }
            )
        }
    }
}

@Parcelize
sealed interface ConversationConfiguration : Parcelable

@Parcelize
data object NothingOpened : ConversationConfiguration

@Parcelize
data class OpenedConversation(val conversationId: Long, val openedThread: OpenedThread?) :
    ConversationConfiguration

@Parcelize
data class OpenedThread(val conversationId: Long, val postId: StandalonePostId) : Parcelable

/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified username.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
data class NavigateToUserConversation(val username: String) : ConversationConfiguration

@Parcelize
private data class AddChannelConfiguration(
    val isCreatingChannel: Boolean,
    val prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration

@Parcelize
private data class CreatePersonalConversation(val prevConfiguration: ConversationConfiguration) :
    ConversationConfiguration

@Parcelize
private data class ConversationSettings(
    val conversationId: Long,
    val prevConfiguration: ConversationConfiguration,
    val isAddingMembers: Boolean = false,
    val isViewingAllMembers: Boolean = false
) : ConversationConfiguration
