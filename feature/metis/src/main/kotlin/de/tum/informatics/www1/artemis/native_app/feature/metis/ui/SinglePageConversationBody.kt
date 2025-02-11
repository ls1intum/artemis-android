package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.feature.metis.AddChannelConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.BrowseChannelConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationSettings
import de.tum.informatics.www1.artemis.native_app.feature.metis.CreatePersonalConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.IgnoreCustomBackHandling
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedSavedPosts
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedThread
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.SavedPostsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.user_conversation.NavigateToUserConversationUi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SinglePageConversationBody(
    modifier: Modifier,
    viewModel: SinglePageConversationBodyViewModel,
    courseId: Long,
    scaffold: @Composable (searchConfiguration: CourseSearchConfiguration, content: @Composable () -> Unit) -> Unit,
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

    val useCustomBackHandling = when {
        configuration.prevConfiguration is IgnoreCustomBackHandling -> false
        configuration is NothingOpened -> false
        else -> true
    }

    BackHandler(useCustomBackHandling) {
        navigateToPrevConfig()
    }

    val conversationOverview: @Composable (Modifier) -> Unit = { m ->
        ConversationOverviewBody(
            modifier = m,
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

    val conversationOverviewViewModel: ConversationOverviewViewModel = koinViewModel { parametersOf(courseId) }
    val query by conversationOverviewViewModel.query.collectAsState()
    val searchConfiguration = CourseSearchConfiguration.Search(
        hint = stringResource(id = R.string.conversation_overview_search_hint),
        query = query,
        onUpdateQuery = conversationOverviewViewModel::onUpdateQuery
    )

    val doAlwaysShowScaffold = getArtemisAppLayout() == ArtemisAppLayout.Tablet
    val scaffoldWrapper = @Composable { content: @Composable () -> Unit ->
        if (doAlwaysShowScaffold) {
            scaffold(searchConfiguration, content)
        } else {
            content()
        }
    }

    scaffoldWrapper {
        AnimatedContent(
            targetState = configuration,
            transitionSpec = {
                val navigationLevelDiff = targetState.navigationLevel - initialState.navigationLevel
                when {
                    navigationLevelDiff > 0 -> DefaultTransition.navigateForward
                    navigationLevelDiff < 0 -> DefaultTransition.navigateBack
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
                    if (doAlwaysShowScaffold) {
                        conversationOverview(modifier)
                    } else {
                        scaffold(searchConfiguration) {
                            conversationOverview(modifier)
                        }
                    }
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
                                openedThread = OpenedThread(postId)
                            )
                        },
                        onCloseThread = navigateToPrevConfig,
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
                    // TODO: This should potentially be moved into the ConversationScreen. That allows us to still display the ConvOverview on the left.
                    //      https://github.com/ls1intum/artemis-android/issues/288
                    SavedPostsScreen(
                        modifier = modifier,
                        courseId = courseId,
                        savedPostStatus = config.status,
                        onNavigateBack = navigateToPrevConfig,
                        onNavigateToPost = { savedPost ->
                            configuration = OpenedConversation(
                                _prevConfiguration = configuration,
                                conversationId = savedPost.conversation.id,
                                openedThread = OpenedThread(
                                    StandalonePostId.ServerSideId(savedPost.referencePostId)
                                )
                            )
                        }
                    )
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
                        navigation = config,
                        onNavigateToConversation = { conversationId ->
                            configuration = OpenedConversation(
                                // We want to skip the NavigateToUserConversationUi when navigating back, as it is only a utility loading screen
                                _prevConfiguration = configuration.prevConfiguration ?: NothingOpened,
                                conversationId = conversationId,
                                openedThread = null
                            )
                        },
                        onNavigateBack = { configuration = NothingOpened }
                    )
                }

                is IgnoreCustomBackHandling -> {
                    throw IllegalStateException("IgnoreCustomBackHandling is only a technical configuration and should not be handled in SinglePageConversationBody")
                }
            }
        }
    }
}
