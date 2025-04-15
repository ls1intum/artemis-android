package de.tum.informatics.www1.artemis.native_app.feature.courseview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.data.CourseServiceFake
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.test.AccountDataServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProviderStub
import de.tum.informatics.www1.artemis.native_app.device.test.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseScaffold
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseTab
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationChatListScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.DataStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl.ChannelServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ConversationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@PlayStoreScreenshots
@Composable
fun `Metis - Conversation Overview`() {
    val course = ScreenshotData.course1

    val viewModel = ConversationOverviewViewModel(
        currentActivityListener = null,
        courseId = 0L,
        conversationService = ConversationServiceStub(
            conversations = ScreenshotCommunicationData.conversations,
        ),
        channelService = ChannelServiceStub,
        serverConfigurationService = ServerConfigurationServiceStub(),
        accountService = AccountServiceStub(),
        conversationPreferenceService = object : ConversationPreferenceService {
            override fun getPreferences(
                serverUrl: String,
                courseId: Long
            ): Flow<ConversationPreferenceService.Preferences> = flowOf(
                ConversationPreferenceService.Preferences(
                    favouritesExpanded = true,
                    generalsExpanded = true,
                    groupChatsExpanded = true,
                    personalConversationsExpanded = true,
                    hiddenExpanded = false,
                    examsExpanded = true,
                    exercisesExpanded = true,
                    lecturesExpanded = true,
                    savedPostsExpanded = false,
                    recentExpanded = true
                ),
            )

            override suspend fun updatePreferences(
                serverUrl: String,
                courseId: Long,
                preferences: ConversationPreferenceService.Preferences
            ) = Unit
        },
        websocketProvider = WebsocketProviderStub(),
        networkStatusProvider = NetworkStatusProviderStub(),
        accountDataService = AccountDataServiceStub(),
        courseService = CourseServiceFake(course)
    )

    ScreenshotFrame(title = "Communicate with students and instructors ...") {
        CourseScaffold(
            modifier = Modifier.fillMaxSize(),
            courseDataState = DataState.Success(course),
            searchConfiguration = CourseSearchConfiguration.Search(
                query = "",
                hint = "Search for a conversation",
                onUpdateQuery = {}
            ),
            isCourseTabSelected = {
                it == CourseTab.Communication
            },
            updateSelectedCourseTab = {},
            onNavigateBack = {},
            onReloadCourse = {},
            collapsingContentState = CollapsingContentState()
        ) {
            ConversationOverviewBody(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToConversation = {},
                onNavigateToSavedPosts = {},
                onRequestCreatePersonalConversation = {},
                onRequestAddChannel = {},
                onRequestBrowseChannel = {},
                collapsingContentState = CollapsingContentState(),
                canCreateChannel = false
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@PlayStoreScreenshots
@Composable
fun `Metis - Conversation Channel`() {
    val conversation = ScreenshotCommunicationData.conversation

    ScreenshotFrame(title = "... by sending and receiving messages directly from the app") {
        ConversationChatListScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = 0L,
            conversationId = conversation.id,
            conversationDataState = DataState.Success(conversation),
            query = "",
            filter = MetisFilter.ALL,
            onUpdateFilter = {},
            onUpdateQuery = {},
            onNavigateBack = {},
            onNavigateToSettings = {},
            conversationDataStatus = DataStatus.UpToDate,
            onRequestSoftReload = {},
            content = { padding ->
                MetisChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding),
                    initialReplyTextProvider = object : InitialReplyTextProvider {
                        override val newMessageText: Flow<TextFieldValue> = flowOf(
                            TextFieldValue()
                        )

                        override fun updateInitialReplyText(text: TextFieldValue) = Unit
                    },
                    posts = PostsDataState.Loaded.WithList(
                        ScreenshotCommunicationData.posts,
                        PostsDataState.NotLoading
                    ),
                    clientId = 0L,
                    postActionFlags = PostActionFlags(
                        isAbleToPin = true,
                        isAtLeastTutorInCourse = true,
                        hasModerationRights = true
                    ),
                    serverUrl = "",
                    courseId = 0,
                    forwardMessageUseCase = mockk<ForwardMessageUseCase>(), // This is a only a workaround
                    state = rememberLazyListState(),
                    isReplyEnabled = true,
                    isMarkedAsDeleteList = mutableStateListOf(),
                    onCreatePost = { CompletableDeferred() },
                    onEditPost = { _, _ -> CompletableDeferred() },
                    onDeletePost = { CompletableDeferred() },
                    onUndoDeletePost = {},
                    onPinPost = { CompletableDeferred() },
                    onSavePost = { CompletableDeferred() },
                    onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                    generateLinkPreviews = { _ -> MutableStateFlow(emptyList()) },
                    onRemoveLinkPreview = { _, _, _ -> CompletableDeferred<MetisModificationFailure>() },
                    bottomItem = null,
                    onClickViewPost = {},
                    onRequestRetrySend = {},
                    onFileSelected = { _ -> },
                    conversationName = conversation.name,
                    emojiService = EmojiServiceStub,
                )
            }
        )
    }
}

