package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.data.AccountDataServiceStub
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.TAB_COMMUNICATION
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.MetisModificationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.MetisStorageServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ConversationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ProvideLocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.lang.RuntimeException
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
                    channelsExpanded = true,
                    groupChatsExpanded = true,
                    personalConversationsExpanded = true,
                    hiddenExpanded = false
                )
            )

            override suspend fun updatePreferences(
                serverUrl: String,
                courseId: Long,
                preferences: ConversationPreferenceService.Preferences
            ) = Unit
        },
        websocketProvider = WebsocketProviderStub(),
        networkStatusProvider = NetworkStatusProviderStub(),
        accountDataService = AccountDataServiceStub()
    )

    val course = DataState.Success(ScreenshotCourse)

    ScreenshotFrame(title = "Communicate with students and instructors") {
        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            courseDataState = course,
            selectedTabIndex = TAB_COMMUNICATION,
            updateSelectedTabIndex = {},
            exerciseTabContent = { },
            lectureTabContent = { },
            communicationTabContent = {
                ConversationOverviewBody(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onNavigateToConversation = {},
                    onRequestCreatePersonalConversation = {},
                    onRequestAddChannel = {}
                )
            },
            onNavigateBack = { },
            onReloadCourse = {}
        )
    }
}

@PlayStoreScreenshots
@Composable
fun `Metis - Conversation Channel`() {
    val context = LocalContext.current
    val emojiService = remember { EmojiServiceImpl(context) }

    startKoin {
        modules(module {
            single<EmojiService> { emojiService }
        })
    }

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

    ScreenshotFrame(title = "Send and receive messages directly from the app") {
        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            courseDataState = DataState.Success(ScreenshotCourse),
            selectedTabIndex = TAB_COMMUNICATION,
            updateSelectedTabIndex = {},
            exerciseTabContent = { },
            lectureTabContent = { },
            communicationTabContent = {
                ProvideLocalVisibleMetisContextManager(
                    visibleMetisContextManager = object : VisibleMetisContextManager {
                        override fun registerMetisContext(metisContext: VisibleMetisContext) =
                            Unit

                        override fun unregisterMetisContext(metisContext: VisibleMetisContext) =
                            Unit
                    }
                ) {
                    ConversationScreen(
                        modifier = Modifier.fillMaxSize(),
                        courseId = ScreenshotCourse.id!!,
                        conversationId = sharedConversation.id,
                        conversationTitle = sharedConversation.humanReadableName,
                        query = "",
                        onUpdateQuery = {},
                        onNavigateBack = {},
                        onNavigateToSettings = {},
                        content = { padding ->
                            MetisChatList(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                                posts = PostsDataState.Loaded.WithList(
                                    posts,
                                    PostsDataState.NotLoading
                                ),
                                isDataOutdated = false,
                                clientId = 0L,
                                hasModerationRights = true,
                                listContentPadding = PaddingValues(),
                                state = rememberLazyListState(),
                                isReplyEnabled = true,
                                onCreatePost = { CompletableDeferred() },
                                onEditPost = { _, _ -> CompletableDeferred() },
                                onDeletePost = { CompletableDeferred() },
                                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                                onClickViewPost = {},
                                onRequestReload = {}
                            )
                        }
                    )
                }
            },
            onNavigateBack = { },
            onReloadCourse = {}
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
            creationDate = time,
            updatedDate = null,
            resolved = false,
            courseWideContext = null,
            tags = emptyList(),
            answers = emptyList(),
            reactions = emptyList()
        )
    )
}