package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.feature.metis.AddChannelConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.BrowseChannelConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationSettings
import de.tum.informatics.www1.artemis.native_app.feature.metis.CreatePersonalConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedSavedPosts
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedThread
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsScreen

@Composable
internal fun SinglePageConversationBody(
    modifier: Modifier,
    viewModel: SinglePageConversationBodyViewModel,
    courseId: Long,
    initialConfiguration: ConversationConfiguration = NothingOpened
) {
    var configuration: ConversationConfiguration by rememberSaveable(initialConfiguration) {
        mutableStateOf(initialConfiguration)
    }

    val openConversation = { conversationId: Long ->
        configuration = OpenedConversation(
            _prevConfiguration = configuration,
            conversationId = conversationId,
            openedThread = null
        )
    }

    val canCreateChannel by viewModel.canCreateChannel.collectAsState()

    val navigateToPrevConfig = {
        when (configuration) {
            NothingOpened -> {}
            else -> configuration = configuration.prevConfiguration ?: NothingOpened
        }
    }

    BackHandler(configuration != NothingOpened) {
        when (val config = configuration) {
            is OpenedConversation -> configuration =
                if (config.openedThread != null) config.copy(openedThread = null) else NothingOpened
            else -> navigateToPrevConfig()
        }
    }

    val conversationOverview: @Composable (Modifier) -> Unit = { m ->
        ConversationOverviewBody(
            modifier = m.padding(top = 16.dp),
            courseId = courseId,
            onNavigateToConversation = openConversation,
            onNavigateToSavedPosts = {
                configuration = OpenedSavedPosts(configuration, it)
            },
            onRequestCreatePersonalConversation = {
                configuration = CreatePersonalConversation(configuration)
            },
            onRequestAddChannel = {
                if (canCreateChannel) {
                    configuration = AddChannelConfiguration(configuration)
                }
            },
            onRequestBrowseChannel = {
                configuration = BrowseChannelConfiguration(configuration)
            },
            canCreateChannel = canCreateChannel
        )
    }

    AnimatedContent(
        targetState = configuration,
        transitionSpec = {
            val navigationLevelDiff = targetState.navigationLevel - initialState.navigationLevel
            when (navigationLevelDiff) {
                1 -> DefaultTransition.navigateForward
                -1 -> DefaultTransition.navigateBack
                else -> DefaultTransition.navigateNeutral
            }
            .using(
                SizeTransform(clip = false)
            )
        },
        contentKey = {
            it.javaClass        // Eg no recomposition of the ChatList when navigating to a thread.
        },
        label = "SinglePageConversationBody screen transition animation"
    ) { config ->
        when (config) {
            NothingOpened -> {
                conversationOverview(modifier)
            }

            is OpenedConversation -> {
                ConversationScreen(
                    modifier = modifier,
                    conversationId = config.conversationId,
                    threadPostId = config.openedThread?.postId,
                    courseId = courseId,
                    onOpenThread = { postId ->
                        configuration = OpenedConversation(
                            _prevConfiguration = config,
                            conversationId = config.conversationId,
                            openedThread = OpenedThread(config.conversationId, postId)
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
                            _prevConfiguration = config
                        )
                    },
                    conversationsOverview = { mod -> conversationOverview(mod) }
                )
            }

            is OpenedSavedPosts -> {
                // TODO
                Text(config.status.name)
            }

            is BrowseChannelConfiguration -> {
                BrowseChannelsScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onNavigateToConversation = openConversation,
                    onNavigateBack = navigateToPrevConfig
                )
            }

            is AddChannelConfiguration -> {
                if (canCreateChannel) {
                    CreateChannelScreen(
                        modifier = modifier,
                        courseId = courseId,
                        onConversationCreated = openConversation,
                        onNavigateBack = navigateToPrevConfig
                    )
                }
            }

            is CreatePersonalConversation -> {
                CreatePersonalConversationScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onConversationCreated = openConversation,
                    onNavigateBack = navigateToPrevConfig
                )
            }

            is ConversationSettings -> {
                when {
                    config.isViewingAllMembers -> {
                        ConversationMembersScreen(
                            modifier = modifier,
                            courseId = courseId,
                            conversationId = config.conversationId,
                            onNavigateBack = navigateToPrevConfig
                        )
                    }

                    config.isAddingMembers -> {
                        ConversationAddMembersScreen(
                            modifier = modifier,
                            courseId = courseId,
                            conversationId = config.conversationId,
                            onNavigateBack = navigateToPrevConfig
                        )
                    }

                    else -> {
                        ConversationSettingsScreen(
                            modifier = modifier,
                            courseId = courseId,
                            conversationId = config.conversationId,
                            onNavigateBack = navigateToPrevConfig,
                            onRequestAddMembers = {
                                configuration = config.copy(
                                    isAddingMembers = true,
                                    _prevConfiguration = configuration
                                )
                            },
                            onRequestViewAllMembers = {
                                configuration = config.copy(
                                    isViewingAllMembers = true,
                                    _prevConfiguration = configuration
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
                        configuration = OpenedConversation(
                            _prevConfiguration = configuration,
                            conversationId = conversationId,
                            openedThread = null
                        )
                    },
                    onNavigateBack = { configuration = NothingOpened }
                )
            }
        }
    }
}
