package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisContentViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel for the standalone view of communication posts. Handles loading of the singular post by the given post id.
 */
internal class MetisThreadViewModel(
    initialPostId: StandalonePostId,
    initialMetisContext: MetisContext,
    subscribeToLiveUpdateService: Boolean,
    private val metisStorageService: MetisStorageService,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    websocketProvider: WebsocketProvider,
    metisContextManager: MetisContextManager,
    metisModificationService: MetisModificationService,
    accountDataService: AccountDataService,
    conversationService: ConversationService,
    replyTextStorageService: ReplyTextStorageService,
    courseService: CourseService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisContentViewModel(
    initialMetisContext,
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

    companion object {
        fun Flow<PostPojo?>.asDataStateFlow(): Flow<DataState<PostPojo>> = map { post ->
            if (post != null) {
                DataState.Success(post)
            } else DataState.Failure(RuntimeException("Post not found"))
        }
    }

    private val postId = MutableStateFlow(initialPostId)

    /**
     * The post data state flow as loading from the server.
     */
    val post: StateFlow<DataState<PostPojo>> = postId.flatMapLatest { postId ->
        when (postId) {
            is StandalonePostId.ClientSideId -> metisStorageService
                .getStandalonePost(postId.clientSideId)
                .asDataStateFlow()

            is StandalonePostId.ServerSideId -> flatMapLatest(
                metisContext,
                serverConfigurationService.serverUrl,
                accountService.authToken,
                onRequestReload.onStart { emit(Unit) }
            ) { metisContext, serverUrl, authToken, _ ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    metisService.getPost(
                        metisContext,
                        serverSidePostId = postId.serverSidePostId,
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                }
            }
                .flatMapLatest { standalonePostDataState ->
                    currentMetisContext.flatMapLatest { metisContext ->
                        handleServerLoadedStandalonePost(metisContext, standalonePostDataState)
                    }
                }
                .onStart { emit(DataState.Loading()) }
        }
    }
        .map { dataState -> dataState.bind { it } } // Type check adaption
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    override suspend fun getPostId(): Long = when (val postId = postId.value) {
        is StandalonePostId.ClientSideId -> metisStorageService.getStandalonePost(postId.clientSideId)
            .filterNotNull().first().serverPostId

        is StandalonePostId.ServerSideId -> postId.serverSidePostId
    }

    private suspend fun handleServerLoadedStandalonePost(
        metisContext: MetisContext,
        standalonePostDataState: DataState<StandalonePost>
    ): Flow<DataState<PostPojo>> {
        val failureFlow: Flow<DataState<PostPojo>> =
            flowOf(DataState.Failure(RuntimeException("Something went wrong while loading the post.")))

        return when (standalonePostDataState) {
            is DataState.Success -> {
                val post = standalonePostDataState.data

                val host = serverConfigurationService.host.first()
                metisStorageService.insertOrUpdatePosts(
                    host = host,
                    metisContext = metisContext,
                    posts = listOf(post),
                    clearPreviousPosts = false
                )

                val clientSidePostId = metisStorageService.getClientSidePostId(
                    host = host,
                    serverSidePostId = post.id ?: 0L,
                    postingType = BasePostingEntity.PostingType.STANDALONE
                )

                if (clientSidePostId != null) {
                    metisStorageService
                        .getStandalonePost(clientSidePostId)
                        .asDataStateFlow()
                } else failureFlow
            }

            is DataState.Failure -> flowOf(DataState.Failure(standalonePostDataState.throwable))
            is DataState.Loading -> flowOf(DataState.Loading())
        }
    }

    /**
     * Wait for the metis context manager to request a refresh. Then load the post from the server.
     */
    init {
        if (subscribeToLiveUpdateService) {
            viewModelScope.launch(coroutineContext) {
                combine(
                    serverConfigurationService.host,
                    metisContext,
                    ::Pair
                ).collectLatest { (host, metisContext) ->
                    metisContextManager.updatePosts(
                        host,
                        metisContext,
                        this@MetisThreadViewModel.coroutineContext
                    )
                }
            }

            viewModelScope.launch(coroutineContext) {
                combine(
                    metisContext,
                    postId,
                    onRequestReload
                ) { a, b, _ -> a to b }
                    .collect { (metisContext, postId) ->
                        val serverSidePostId = when (postId) {
                            is StandalonePostId.ClientSideId -> {
                                post.filterSuccess().filterNotNull().first().serverPostId
                            }

                            is StandalonePostId.ServerSideId -> postId.serverSidePostId
                        }

                        val serverUrl = serverConfigurationService.serverUrl.first()
                        val host = serverConfigurationService.host.first()
                        val authToken = when (val authData =
                            accountService.authenticationData.first()) {
                            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                            AccountService.AuthenticationData.NotLoggedIn -> return@collect
                        }

                        retryOnInternetIndefinetly(
                            networkStatusProvider.currentNetworkStatus,
                        ) {
                            metisService.getPost(
                                metisContext,
                                serverSidePostId,
                                serverUrl,
                                authToken
                            )
                        }
                            .filterSuccess()
                            .collect { post ->
                                metisStorageService.updatePost(
                                    host,
                                    metisContext,
                                    post
                                )
                            }
                    }
            }
        }
    }

    fun createReply(): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            if (!post.value.isSuccess) return@async MetisModificationFailure.CREATE_POST

            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.CREATE_POST

            val replyPost = AnswerPost(
                creationDate = Clock.System.now(),
                content = newMessageText.first().text,
                post = StandalonePost(
                    id = when (val postId = postId.value) {
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

    fun updatePostId(newPostId: StandalonePostId) {
        postId.value = newPostId

        viewModelScope.launch(coroutineContext) {
            newMessageText.value = TextFieldValue(retrieveNewMessageText(metisContext.value, getPostId()))
        }
    }
}
