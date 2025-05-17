package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.R.drawable.sidebar_icon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui.SavedPostsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
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
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductFacadeUi

private const val TAG = "SinglePageConversationBody"

@Composable
fun SinglePageConversationBody(
    modifier: Modifier,
    courseId: Long,
    scaffold: @Composable (searchConfiguration: CourseSearchConfiguration, content: @Composable () -> Unit) -> Unit,
    collapsingContentState: CollapsingContentState,
    initialConfiguration: ConversationConfiguration = NothingOpened,
    title: String
) {
    val viewModel: SinglePageConversationBodyViewModel = koinViewModel { parametersOf(courseId) }
    var configuration: ConversationConfiguration by rememberSaveable(initialConfiguration) {
        mutableStateOf(initialConfiguration)
    }

    val layout = getArtemisAppLayout()
    var isSidebarOpen by rememberSaveable { mutableStateOf(true) }
    val isTabletPortrait = layout.isTabletPortrait

    val openConversation = { conversationId: Long ->
        if (isTabletPortrait) isSidebarOpen = false
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
            collapsingContentState = collapsingContentState,
            onNavigateToConversation = openConversation,
            onNavigateToSavedPosts = {
                configuration = OpenedSavedPosts(configuration)
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
            canCreateChannel = canCreateChannel,
            selectedConversationId = (configuration as? OpenedConversation)?.conversationId,
        )
    }

    var showCodeOfConduct by remember { mutableStateOf(true) }
    val conversationOverviewViewModel: ConversationOverviewViewModel =
        koinViewModel { parametersOf(courseId) }
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

    if (showCodeOfConduct) {
        scaffold(CourseSearchConfiguration.DisabledSearch) {
            CodeOfConductFacadeUi(
                modifier = modifier,
                courseId = courseId,
                onCodeOfConductAccepted = {
                    showCodeOfConduct = false
                }
            )
        }
    } else {
        scaffoldWrapper {
            AnimatedContent(
                targetState = configuration,
                transitionSpec = {
                    if (doAlwaysShowScaffold) {
                        DefaultTransition.navigateNeutral
                    } else {
                        val navigationLevelDiff =
                            targetState.navigationLevel - initialState.navigationLevel
                        when {
                            navigationLevelDiff > 0 -> DefaultTransition.navigateForward
                            navigationLevelDiff < 0 -> DefaultTransition.navigateBack
                            else -> DefaultTransition.navigateNeutral
                        }
                            .using(
                                SizeTransform(clip = false)
                            )
                    }
                },
                contentKey = {
                    it.javaClass        // Eg no recomposition of the ChatList when navigating to a thread.
                },
                label = "SinglePageConversationBody screen transition animation"
            ) { config ->
                // This handles the state of the search bar in tablet mode depending on the view
                // We only want to show it in the conversation overview
                if (doAlwaysShowScaffold) {
                    if (config is NothingOpened) {
                        collapsingContentState.resetCollapsingContent()
                    } else {
                        collapsingContentState.collapseContent()
                    }
                }

                LayoutAwareTwoColumnLayout(
                    modifier = modifier,
                    isSidebarOpen = isSidebarOpen,
                    onSidebarToggle = { isSidebarOpen = !isSidebarOpen },
                    optionalColumn = conversationOverview,
                    priorityColumn = { innerMod ->
                        ConversationContent(
                            modifier = innerMod,
                            configuration = config,
                            onUpdateConfig = { configuration = it },
                            onNavigateBack = navigateToPrevConfig,
                            courseId = courseId,
                            canCreateChannel = canCreateChannel,
                            onSidebarToggle = { isSidebarOpen = !isSidebarOpen },
                            scaffold = { content -> scaffold(searchConfiguration, content) },
                            conversationOverview = conversationOverview
                        )
                    },
                    title = title
                )

            }
        }
    }

}

@Composable
private fun ConversationContent(
    modifier: Modifier,
    configuration: ConversationConfiguration,
    onUpdateConfig: (ConversationConfiguration) -> Unit,
    onNavigateBack: () -> Unit,
    courseId: Long,
    canCreateChannel: Boolean,
    onSidebarToggle: () -> Unit,
    scaffold: @Composable (content: @Composable () -> Unit) -> Unit,
    conversationOverview: @Composable (Modifier) -> Unit
) {
    when (configuration) {
        NothingOpened -> {
            if (getArtemisAppLayout() == ArtemisAppLayout.Phone) {
                scaffold {
                    conversationOverview(modifier)
                }
            } else {
                IconButton(onClick = onSidebarToggle) {
                    Icon(
                        painter = painterResource(id = sidebar_icon),
                        contentDescription = null
                    )
                }
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Please select a conversation from the list.")
                }
            }
        }

        is OpenedConversation -> {
            ConversationScreen(
                modifier = modifier,
                conversationId = configuration.conversationId,
                threadPostId = configuration.openedThread?.postId,
                courseId = courseId,
                onOpenThread = { postId ->
                    onUpdateConfig(
                        OpenedConversation(
                            configuration,
                            configuration.conversationId,
                            OpenedThread(postId)
                        )
                    )
                },
                onCloseThread = onNavigateBack,
                onCloseConversation = { onUpdateConfig(NothingOpened) },
                onNavigateToSettings = {
                    onUpdateConfig(
                        ConversationSettings(
                            configuration.conversationId,
                            configuration
                        )
                    )
                },
                onSidebarToggle = onSidebarToggle
            )
        }

        is OpenedSavedPosts -> {
            SavedPostsScreen(
                modifier = modifier,
                courseId = courseId,
                onNavigateToPost = { savedPost ->
                    onUpdateConfig(
                        OpenedConversation(
                            configuration,
                            savedPost.conversation.id,
                            OpenedThread(StandalonePostId.ServerSideId(savedPost.referencePostId))
                        )
                    )
                },
                onSidebarToggle = onSidebarToggle
            )
        }

        is BrowseChannelConfiguration -> {
            BrowseChannelsScreen(
                modifier = modifier,
                onNavigateToConversation = {
                    onUpdateConfig(
                        OpenedConversation(
                            configuration,
                            it,
                            null
                        )
                    )
                },
                onNavigateBack = onNavigateBack,
                onSidebarToggle = onSidebarToggle
            )
        }

        is AddChannelConfiguration -> {
            if (canCreateChannel) {
                CreateChannelScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onConversationCreated = {
                        onUpdateConfig(
                            OpenedConversation(
                                configuration,
                                it,
                                null
                            )
                        )
                    },
                    onNavigateBack = onNavigateBack,
                    onSidebarToggle = onSidebarToggle
                )
            }
        }

        is CreatePersonalConversation -> {
            CreatePersonalConversationScreen(
                modifier = modifier,
                courseId = courseId,
                onConversationCreated = {
                    onUpdateConfig(
                        OpenedConversation(
                            configuration,
                            it,
                            null
                        )
                    )
                },
                onNavigateBack = onNavigateBack,
                onSidebarToggle = onSidebarToggle
            )
        }

        is ConversationSettings -> {
            when {
                configuration.isViewingAllMembers -> ConversationMembersScreen(
                    modifier = modifier,
                    courseId = courseId,
                    conversationId = configuration.conversationId,
                    onNavigateBack = onNavigateBack,
                    onSidebarToggle = onSidebarToggle
                )

                configuration.isAddingMembers -> ConversationAddMembersScreen(
                    modifier = modifier,
                    courseId = courseId,
                    conversationId = configuration.conversationId,
                    onNavigateBack = onNavigateBack,
                    onSidebarToggle = onSidebarToggle
                )

                else -> ConversationSettingsScreen(
                    modifier = modifier,
                    courseId = courseId,
                    conversationId = configuration.conversationId,
                    onNavigateBack = onNavigateBack,
                    onSidebarToggle = onSidebarToggle,
                    onRequestAddMembers = {
                        onUpdateConfig(
                            configuration.copy(
                                isAddingMembers = true,
                                _prevConfiguration = configuration
                            )
                        )
                    },
                    onRequestViewAllMembers = {
                        onUpdateConfig(
                            configuration.copy(
                                isViewingAllMembers = true,
                                _prevConfiguration = configuration
                            )
                        )
                    },
                    onConversationLeft = { onUpdateConfig(NothingOpened) },
                    onChannelDeleted = { onUpdateConfig(NothingOpened) }
                )
            }
        }

        is NavigateToUserConversation -> NavigateToUserConversationUi(
            modifier = modifier,
            courseId = courseId,
            navigation = configuration,
            onNavigateToConversation = {
                onUpdateConfig(
                    OpenedConversation(configuration.prevConfiguration ?: NothingOpened, it, null)
                )
            },
            onNavigateBack = { onUpdateConfig(NothingOpened) },
            onSidebarToggle = onSidebarToggle
        )

        is IgnoreCustomBackHandling -> {
            Log.e(TAG, "IgnoreCustomBackHandling is not meant to be displayed")
            onUpdateConfig(NothingOpened)
        }
    }
}
