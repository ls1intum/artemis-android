package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisContentViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for the standalone view of communication posts. Handles loading of the singular post by the given post id.
 */
internal class MetisThreadViewModel(
    val postId: StandalonePostId,
    val metisContext: MetisContext,
    private val metisStorageService: MetisStorageService,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    subscribeToLiveUpdateService: Boolean,
    websocketProvider: WebsocketProvider,
    metisContextManager: MetisContextManager,
    metisModificationService: MetisModificationService,
    serverDataService: ServerDataService,
    conversationService: ConversationService
) : MetisContentViewModel(
    metisContext,
    websocketProvider,
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider,
    conversationService
) {

    companion object {
        fun Flow<Post?>.asDataStateFlow(): Flow<DataState<Post>> = map { post ->
            if (post != null) {
                DataState.Success(post)
            } else DataState.Failure(RuntimeException("Post not found"))
        }
    }

    /**
     * The post data state flow as loading from the server.
     */
    val post: StateFlow<DataState<Post>> = when (postId) {
        is StandalonePostId.ClientSideId -> metisStorageService
            .getStandalonePost(postId.clientSideId)
            .asDataStateFlow()

        is StandalonePostId.ServerSideId -> flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            onRequestReload.onStart { emit(Unit) }
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
            .flatMapLatest { standalonePostDataState ->
                handleServerLoadedStandalonePost(standalonePostDataState)
            }
            .onStart { emit(DataState.Loading()) }
    }
        .map { dataState -> dataState.bind { it } } // Type check adaption
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    private suspend fun handleServerLoadedStandalonePost(standalonePostDataState: DataState<StandalonePost>): Flow<DataState<Post>> {
        val failureFlow: Flow<DataState<Post>> =
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
            viewModelScope.launch {
                serverConfigurationService.host.collectLatest { host ->
                    metisContextManager.updatePosts(host, metisContext)
                }
            }

            viewModelScope.launch {
                onRequestReload
                    .collect {
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

    fun createReply(replyText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            if (!post.value.isSuccess) return@async MetisModificationFailure.CREATE_POST

            val conversation = loadConversation() ?: return@async MetisModificationFailure.CREATE_POST

            val replyPost = AnswerPost(
                creationDate = Clock.System.now(),
                content = replyText,
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

    override suspend fun getMetisContext(): MetisContext = metisContext
}