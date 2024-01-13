package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ConversationChatListUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisContentViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ConversationThreadUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class ConversationViewModel(
    val courseId: Long,
    val conversationId: Long,
    initialPostId: StandalonePostId?,
    metisService: MetisService,
    websocketProvider: WebsocketProvider,
    metisModificationService: MetisModificationService,
    metisStorageService: MetisStorageService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    conversationService: ConversationService,
    replyTextStorageService: ReplyTextStorageService,
    courseService: CourseService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisContentViewModel(
    MetisContext.Conversation(courseId, conversationId),
    websocketProvider,
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    conversationService,
    replyTextStorageService,
    courseService,
    coroutineContext
) {

    val metisContext = MetisContext.Conversation(courseId, conversationId)

    private val _postId: MutableStateFlow<StandalonePostId?> = MutableStateFlow(initialPostId)
    val postId: StateFlow<StandalonePostId?> = _postId

    val chatListUseCase = ConversationChatListUseCase(
        viewModelScope = viewModelScope,
        metisService = metisService,
        metisStorageService = metisStorageService,
        metisContext = metisContext,
        onRequestReload = onRequestReload,
        clientIdOrDefault = clientIdOrDefault,
        serverConfigurationService = serverConfigurationService,
        accountService = accountService
    )

    val threadUseCase = ConversationThreadUseCase(
        metisContext = metisContext,
        postId = postId.filterNotNull(),
        onRequestReload = onRequestReload,
        viewModelScope = viewModelScope,
        metisStorageService = metisStorageService,
        metisService = metisService,
        networkStatusProvider = networkStatusProvider,
        serverConfigurationService = serverConfigurationService,
        accountService = accountService,
        coroutineContext = coroutineContext
    )

    init {
        // Reload the newMessageText whenever postId changes
        viewModelScope.launch(coroutineContext) {
            postId.collect { _ ->
                newMessageText.value = TextFieldValue(
                    retrieveNewMessageText(
                        metisContext,
                        getPostId()
                    )
                )
            }
        }
    }

    override suspend fun getPostId(): Long? = when (val postId = postId.value) {
        is StandalonePostId.ClientSideId -> metisStorageService.getStandalonePost(postId.clientSideId)
            .filterNotNull().first().serverPostId

        is StandalonePostId.ServerSideId -> postId.serverSidePostId
        null -> null
    }

    fun createPost(): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val postText = newMessageText.value.text

            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.CREATE_POST

            val post = StandalonePost(
                id = null,
                title = null,
                tags = null,
                content = postText,
                conversation = conversation,
                creationDate = Clock.System.now(),
                displayPriority = DisplayPriority.NONE
            )

            createStandalonePostImpl(post)
        }
    }

    fun createReply(): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val postId = postId.value
            if (postId == null) return@async MetisModificationFailure.CREATE_POST

            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.CREATE_POST

            val replyPost = AnswerPost(
                creationDate = Clock.System.now(),
                content = newMessageText.first().text,
                post = StandalonePost(
                    id = when (postId) {
                        is StandalonePostId.ClientSideId -> metisStorageService.getServerSidePostId(
                            serverConfigurationService.host.first(),
                            postId.clientSideId
                        )

                        is StandalonePostId.ServerSideId -> postId.serverSidePostId
                    },
                    conversation = conversation
                )
            )

            createAnswerPostImpl(replyPost)
        }
    }

    fun updateOpenedThread(newPostId: StandalonePostId?) {
        _postId.value = newPostId
    }
}