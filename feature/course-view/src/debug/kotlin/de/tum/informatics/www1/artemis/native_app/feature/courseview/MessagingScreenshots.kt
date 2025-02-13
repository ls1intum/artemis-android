package de.tum.informatics.www1.artemis.native_app.feature.courseview

import android.annotation.SuppressLint
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
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProviderStub
import de.tum.informatics.www1.artemis.native_app.device.test.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseScaffold
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseTab
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationChatListScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.DataStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ConversationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

private val sharedConversation = ChannelChat(
    id = 1L,
    name = "Designing a rocket engine - Q&A",
    unreadMessagesCount = 17,
)

@PlayStoreScreenshots
@Composable
fun `Metis - Conversation Overview`() {
    val viewModel = ConversationOverviewViewModel(
        currentActivityListener = null,
        courseId = 0L,
        conversationService = ConversationServiceStub(
            conversations = listOf(
                ChannelChat(
                    id = 0L,
                    name = "General Course Questions",
                    unreadMessagesCount = 8,
                ),
                sharedConversation,
                GroupChat(
                    id = 2L,
                    name = "Team Rocket Students"
                ),
                OneToOneChat(
                    id = 3L,
                    members = listOf(
                        ConversationUser(
                            username = "u1",
                            firstName = "Ethan",
                            lastName = "Martin"
                        )
                    )
                ),
                OneToOneChat(
                    id = 4L,
                    members = listOf(
                        ConversationUser(
                            username = "u2",
                            firstName = "Sophia",
                            lastName = "Davis"
                        )
                    ),
                    unreadMessagesCount = 3,
                )
            )
        ),
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
        courseService = CourseServiceFake(ScreenshotCourse)
    )

    val course = DataState.Success(ScreenshotCourse)

    ScreenshotFrame(title = "Communicate with students and instructors") {
        CourseScaffold(
            modifier = Modifier.fillMaxSize(),
            courseDataState = course,
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
            collapsingContentState = CollapsingContentState(
                0f, 0f, true
            )
        ) {
            ConversationOverviewBody(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToConversation = {},
                onNavigateToSavedPosts = {},
                onRequestCreatePersonalConversation = {},
                onRequestAddChannel = {},
                onRequestBrowseChannel = {},
                collapsingContentState = CollapsingContentState(
                    0f, 0f, true
                ),
                canCreateChannel = false
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@PlayStoreScreenshots
@Composable
fun `Metis - Conversation Channel`() {
    val date = LocalDate(2023, 7, 29)
    val firstMessageTime = date.atTime(13, 34).toInstant(TimeZone.UTC)

    val posts = listOf(
        ChatListItem.DateDivider(date),
        generateMessage(
            name = "Sam",
            text = "Hey, folks! What are the big advantages of solid chemical propellants in rockets?",
            time = firstMessageTime,
            id = "1",
            authorId = 0L
        ),
        generateMessage(
            name = "Mia",
            text = "Hey, Sam! Solid propellants are known for their simplicity and reliability. They're easy to handle.",
            time = firstMessageTime + 3.minutes,
            id = "2",
            authorId = 1L
        ),
        generateMessage(
            name = "Ethan",
            text = "That's right, Mia. They have a consistent burn rate and a good thrust-to-weight ratio, which makes them handy for various missions.",
            time = firstMessageTime + 12.minutes,
            id = "3",
            authorId = 2L
        ),
        generateMessage(
            name = "Sam",
            text = " Thanks, Mia and Ethan! So, they're like the dependable workhorses of rocket propellants, huh?",
            time = firstMessageTime + 15.minutes,
            id = "4",
            authorId = 0L
        ),
    ).reversed()

    // TODO: Provide artemis image provider
    ScreenshotFrame(title = "Send and receive messages directly from the app") {
        ConversationChatListScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = ScreenshotCourse.id!!,
            conversationId = sharedConversation.id,
            conversationDataState = DataState.Success(sharedConversation),
            query = "",
            onUpdateQuery = {},
            onNavigateBack = {},
            onNavigateToSettings = {},
            conversationDataStatus = DataStatus.UpToDate,
            onRequestSoftReload = {},
            content = { padding ->
                MetisChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    initialReplyTextProvider = object : InitialReplyTextProvider {
                        override val newMessageText: Flow<TextFieldValue> = flowOf(
                            TextFieldValue()
                        )

                        override fun updateInitialReplyText(text: TextFieldValue) = Unit
                    },
                    posts = PostsDataState.Loaded.WithList(
                        posts,
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
                    bottomItem = null,
                    onClickViewPost = {},
                    onRequestRetrySend = {},
                    onFileSelected = { _ -> },
                    conversationName = "Chat",
                    emojiService = EmojiServiceStub,
                )
            }
        )
    }
}

private fun generateMessage(
    name: String,
    text: String,
    time: Instant,
    id: String,
    authorId: Long
): ChatListItem.PostChatListItem {
    return ChatListItem.PostChatListItem(
        PostPojo(
            clientPostId = id,
            serverPostId = 0L,
            title = null,
            content = text,
            authorName = name,
            authorRole = UserRole.USER,
            authorId = authorId,
            authorImageUrl = null,
            creationDate = time,
            updatedDate = null,
            resolved = false,
            isSaved = false,
            courseWideContext = null,
            tags = emptyList(),
            answers = emptyList(),
            reactions = emptyList(),
            displayPriority = null
        )
    )
}