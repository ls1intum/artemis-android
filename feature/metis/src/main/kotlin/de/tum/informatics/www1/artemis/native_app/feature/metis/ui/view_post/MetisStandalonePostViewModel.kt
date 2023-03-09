package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.*
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisContentViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class MetisStandalonePostViewModel(
    private val postId: StandalonePostId,
    private val metisContext: MetisContext,
    subscribeToLiveUpdateService: Boolean,
    private val websocketProvider: WebsocketProvider,
    private val metisStorageService: MetisStorageService,
    metisContextManager: MetisContextManager,
    private val metisService: MetisService,
    metisModificationService: MetisModificationService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverDataService: ServerDataService
) : MetisContentViewModel(
    websocketProvider,
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider
) {

    companion object {
        fun Flow<Post?>.asDataStateFlow(): Flow<DataState<Post>> = map { post ->
            if (post != null) {
                DataState.Success(post)
            } else DataState.Failure(RuntimeException("Post not found"))
        }
    }

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
                val failureFlow: Flow<DataState<Post>> =
                    flowOf(DataState.Failure(RuntimeException("Something went wrong while loading the post.")))

                when (standalonePostDataState) {
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
            .onStart { emit(DataState.Loading()) }
    }
        .map { dataState -> dataState.bind { it } } // Type check adaption
        .stateIn(viewModelScope, SharingStarted.Eagerly)

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

    fun requestWebsocketReload() {
        viewModelScope.launch {
            websocketProvider.requestTryReconnect()
        }
    }

    /**
     * Convenience method that wraps the superclass create reaction.
     */
    fun createReactionForAnswer(
        emojiId: String,
        clientSidePostId: String,
        onResponse: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            val serverSidePostId = metisStorageService.getServerSidePostId(
                serverConfigurationService.host.first(),
                clientSidePostId
            )

            createReactionImpl(
                emojiId = emojiId,
                post = MetisModificationService.AffectedPost.Answer(postId = serverSidePostId),
                response = onResponse
            )
        }
    }

    fun onClickReactionOfAnswer(
        emojiId: String,
        clientSidePostId: String,
        presentReactions: List<Post.Reaction>,
        onResponse: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            val serverSidePostId = metisStorageService.getServerSidePostId(
                serverConfigurationService.host.first(),
                clientSidePostId
            )

            onClickReactionImpl(
                emojiId = emojiId,
                post = MetisModificationService.AffectedPost.Answer(postId = serverSidePostId),
                presentReactions = presentReactions,
                response = onResponse
            )
        }
    }

    fun createReply(
        replyText: String,
        onResponse: (MetisModificationFailure?) -> Unit
    ): Job {
        return viewModelScope.launch {
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
                    }
                )
            )

            createAnswerPostImpl(replyPost, onResponse)
        }
    }

    override suspend fun getMetisContext(): MetisContext = metisContext
}