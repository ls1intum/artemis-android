package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.util.ForwardedMessagesHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.DataStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


private const val TAG = "ConversationThreadUseCase"

/**
 * ViewModel for the standalone view of communication posts. Handles loading of the singular post by the given post id.
 */
internal class ConversationThreadUseCase(
    metisContext: MetisContext,
    postId: Flow<StandalonePostId>,
    onRequestSoftReload: Flow<Unit>,
    private val viewModelScope: CoroutineScope,
    private val metisStorageService: MetisStorageService,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val courseId: Long
) {

    companion object {
        fun Flow<PostPojo?>.asDataStateFlow(): Flow<DataState<PostPojo>> = map { post ->
            if (post != null) {
                DataState.Success(post)
            } else DataState.Failure(RuntimeException("Post not found"))
        }
    }

    private val forwardedMessagesHandler: StateFlow<ForwardedMessagesHandler> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken
    ) { serverUrl, authToken ->
        flowOf(
            ForwardedMessagesHandler(
                metisService = metisService,
                metisContext = metisContext,
                authToken = authToken,
                serverUrl = serverUrl
            )
        )
    }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, ForwardedMessagesHandler(metisService, metisContext, "", ""))

    /**
     * The post data state flow as loading from the server.
     */
    val post: StateFlow<DataState<IStandalonePost>> = postId.flatMapLatest { postId ->
        Log.d(TAG, "ConversationThreadUseCase loading with postId: $postId")
        when (postId) {
            is StandalonePostId.ClientSideId -> metisStorageService
                .getStandalonePost(postId.clientSideId)
                .asDataStateFlow()

            is StandalonePostId.ServerSideId -> flatMapLatest(
                serverConfigurationService.serverUrl,
                accountService.authToken,
                onRequestSoftReload.onStart { emit(Unit) }
            ) { serverUrl, authToken, _ ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    metisService.getPost(
                        metisContext,
                        serverSidePostId = postId.serverSidePostId,
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                }
            }
                // Also listens to the database in the end.
                .flatMapLatest { standalonePostDataState ->
                    handleServerLoadedStandalonePost(metisContext, standalonePostDataState)
                }
                .onStart { emit(DataState.Loading()) }
        }
    }
        .map { dataState -> dataState.bind { it } } // Type check adaption
        .combine(forwardedMessagesHandler) { postDataState, forwardedMessagesHandler ->
            loadForwardedMessages(postDataState, forwardedMessagesHandler)
            postDataState
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val dataStatus: Flow<DataStatus> = post.map { dataState ->
        when (dataState) {
            is DataState.Failure -> DataStatus.Outdated
            is DataState.Loading -> DataStatus.Loading
            is DataState.Success -> DataStatus.UpToDate
        }
    }

    val chatListItem: StateFlow<ChatListItem.PostItem.ThreadItem?> = combine(
        post,
        forwardedMessagesHandler
    ) { postDataState, forwardedMessagesHandler ->
        when (postDataState) {
            is DataState.Success -> {
                val standalonePost = postDataState.data
                if (standalonePost.hasForwardedMessages == true) {
                    forwardedMessagesHandler.resolveForwardedMessagesForThreadPost(
                        chatListItem = ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage(
                            post = standalonePost,
                            forwardedPosts = emptyList(),
                            courseId = courseId
                        )
                    )
                } else {
                    ChatListItem.PostItem.ThreadItem.ContextItem.ContextPost(standalonePost)
                }
            }

            is DataState.Loading -> null
            is DataState.Failure -> null
        }
    }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, null)

    fun getAnswerChatListItem(answerPost: IAnswerPost): StateFlow<ChatListItem.PostItem.ThreadItem.Answer?> =
        forwardedMessagesHandler.flatMapLatest { forwardedMessagesHandler ->
            flow {
                val chatListItem = if (answerPost.hasForwardedMessages == true) {
                    forwardedMessagesHandler.resolveForwardedMessagesForThreadPost(
                        chatListItem = ChatListItem.PostItem.ThreadItem.Answer.AnswerPostWithForwardedMessage(
                            post = answerPost,
                            forwardedPosts = emptyList(),
                            courseId = courseId
                        )
                    ) as ChatListItem.PostItem.ThreadItem.Answer
                } else {
                    ChatListItem.PostItem.ThreadItem.Answer.AnswerPost(answerPost)
                }
                emit(chatListItem)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Handles when the post has been directly loaded from the server. Stores the resulting post in the db.
     */
    private suspend fun handleServerLoadedStandalonePost(
        metisContext: MetisContext,
        standalonePostDataState: DataState<StandalonePost>
    ): Flow<DataState<out IStandalonePost>> {
        val failureFlow: Flow<DataState<IStandalonePost>> =
            flowOf(DataState.Failure(RuntimeException("Something went wrong while loading the post.")))

        return when (standalonePostDataState) {
            is DataState.Success -> {
                // The transaction is needed to ensure that storing the post in the db and re-fetching it
                // does not interfere with other transactions. Eg, when reloading, the ConversationChatListUseCase
                // might delete older updated posts.
                metisStorageService.withTransaction {
                    val post = standalonePostDataState.data

                    val host = serverConfigurationService.host.first()
                    metisStorageService.insertOrUpdatePosts(
                        host = host,
                        metisContext = metisContext,
                        posts = listOf(post)
                    )

                    val clientSidePostId = metisStorageService.getClientSidePostId(
                        host = host,
                        serverSidePostId = post.id ?: 0L,
                        postingType = BasePostingEntity.PostingType.STANDALONE
                    )

                    if (clientSidePostId != null) {
                        val storedPost = metisStorageService
                            .getStandalonePost(clientSidePostId).first()

                        flowOf(storedPost).asDataStateFlow()
                    } else failureFlow
                }
            }

            is DataState.Failure -> flowOf(DataState.Failure(standalonePostDataState.throwable))
            is DataState.Loading -> flowOf(DataState.Loading())
        }
    }

    private suspend fun loadForwardedMessages(
        postDataState: DataState<IStandalonePost>,
        forwardedMessagesHandler: ForwardedMessagesHandler
    ) {
        if (postDataState is DataState.Success) {
            val post = postDataState.data

            if (post.hasForwardedMessages == true) {
                forwardedMessagesHandler.extractForwardedMessages(listOf(post))
                forwardedMessagesHandler.loadForwardedMessages(PostingType.POST)
            }

            if (post.answers.orEmpty().isNotEmpty()) {
                forwardedMessagesHandler.extractForwardedMessages(post.answers.orEmpty())
                forwardedMessagesHandler.loadForwardedMessages(PostingType.ANSWER)
            }
        }
    }
}
