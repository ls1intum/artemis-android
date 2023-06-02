package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.holdLatestLoaded
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationWebsocketDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.AnswerPostDb
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

/**
 * Common viewModel for metis viewModels that display live metis content.
 * Live metis content is content that is being permanently updated by websockets.
 */
abstract class MetisContentViewModel(
    metisContext: MetisContext,
    private val websocketProvider: WebsocketProvider,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider,
    websocketProvider
) {

    val hasModerationRights: StateFlow<Boolean> = when (metisContext) {
        is MetisContext.Conversation -> {
            flatMapLatest(
                serverConfigurationService.serverUrl,
                accountService.authToken,
                onRequestReload.onStart { emit(Unit) }
            ) { serverUrl, authToken, _ ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    conversationService
                        .getConversation(
                            courseId = metisContext.courseId,
                            conversationId = metisContext.conversationId,
                            authToken = serverUrl,
                            serverUrl = authToken
                        )
                        .bind { it.hasModerationRights }
                }
                    .map { it.orElse(false) }
            }
        }

        else -> flowOf(false)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Emits true if the data may be outdated. Listens to the connection state of the websocket
     * If a connection was established and is broken, then the data may be corrupted. A reload resets this
     */
    val isDataOutdated: StateFlow<Boolean> = onRequestReload
        .onStart { emit(Unit) }
        .transformLatest {
            emit(false)
            var wasConnected = false

            websocketProvider.connectionState.collect { connectionState ->
                when (connectionState) {
                    is WebsocketProvider.WebsocketConnectionState.WithSession -> {
                        if (!wasConnected && connectionState.isConnected) {
                            wasConnected = true
                        } else if (wasConnected && !connectionState.isConnected) {
                            emit(true)
                        }
                    }

                    WebsocketProvider.WebsocketConnectionState.Empty -> {
                        wasConnected = false
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val conversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onReloadRequestAndWebsocketReconnect.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        when (metisContext) {
            is MetisContext.Conversation -> retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.getConversation(
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    authToken = authToken,
                    serverUrl = serverUrl
                )
            }

            else -> flowOf(DataState.Failure(RuntimeException("Not a conversation")))
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    val latestUpdatedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        conversation.holdLatestLoaded(),
        clientId.filterSuccess()
    ) { conversationDataState, clientId ->
        websocketProvider.subscribeToConversationUpdates(clientId, metisContext.courseId)
            .filter { it.crudAction == MetisPostAction.UPDATE }
            .map<ConversationWebsocketDTO, DataState<Conversation>> { DataState.Success(it.conversation) }
            .onStart { emit(conversationDataState) }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Handles a reaction click. If the client has already reacted, it deletes the reaction.
     * Otherwise it creates a reaction with the same emoji id.
     */
    fun createOrDeleteReaction(
        post: IBasePost,
        emojiId: String,
        create: Boolean
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            if (create) {
                createReactionImpl(emojiId, post.asAffectedPost)
            } else {
                val clientId =
                    clientId.value.orNull() ?: return@async MetisModificationFailure.DELETE_REACTION

                val exitingReactionId = post
                    .reactions
                    .orEmpty()
                    .filter { it.emojiId == emojiId }
                    .firstOrNull { it.creatorId == clientId }
                    ?.id
                    ?: return@async MetisModificationFailure.DELETE_REACTION

                deleteReactionImpl(exitingReactionId)
            }
        }
    }

    private suspend fun createReactionImpl(
        emojiId: String,
        post: MetisModificationService.AffectedPost
    ): MetisModificationFailure? {
        val networkResponse: NetworkResponse<Reaction> = metisModificationService.createReaction(
            context = getMetisContext(),
            post = post,
            emojiId = emojiId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return networkResponse.bind<MetisModificationFailure?> { null }
            .or(MetisModificationFailure.CREATE_REACTION)
    }

    private suspend fun deleteReactionImpl(reactionId: Long): MetisModificationFailure? {
        val success = metisModificationService.deleteReaction(
            context = getMetisContext(),
            reactionId = reactionId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        ).or(false)

        return if (success) null else MetisModificationFailure.DELETE_REACTION
    }

    protected suspend fun createStandalonePostImpl(post: StandalonePost): MetisModificationFailure? {
        val metisContext = getMetisContext()
        val response = metisModificationService.createPost(
            context = metisContext,
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return when (response) {
            is NetworkResponse.Failure -> MetisModificationFailure.CREATE_POST
            is NetworkResponse.Response -> {
                metisStorageService.insertLiveCreatedPost(
                    serverConfigurationService.host.first(),
                    metisContext,
                    response.data
                )

                null
            }
        }
    }

    protected suspend fun createAnswerPostImpl(post: AnswerPost): MetisModificationFailure? {
        val response = metisModificationService.createAnswerPost(
            context = getMetisContext(),
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return response.bind<MetisModificationFailure?> { null }
            .or(MetisModificationFailure.CREATE_POST)
    }

    fun deletePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            metisModificationService.deletePost(
                getMetisContext(),
                post.asAffectedPost,
                serverConfigurationService.serverUrl.first(),
                accountService.authToken.first()
            )
                .bind { if (it) null else MetisModificationFailure.DELETE_POST }
                .or(MetisModificationFailure.DELETE_POST)
        }
    }

    fun editPost(post: Post, newText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val newPost = StandalonePost(
                post = post.copy(content = newText),
                conversation = conversation
            )

            metisModificationService.updateStandalonePost(
                context = getMetisContext(),
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun editAnswerPost(
        parentPost: Post,
        post: AnswerPostDb,
        newText: String
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val serializedParentPost = StandalonePost(parentPost, conversation)
            val newPost = AnswerPost(post, serializedParentPost).copy(content = newText)

            metisModificationService.updateAnswerPost(
                context = getMetisContext(),
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    /**
     * Emits to onRequestReload. If the websocket is currently not connected, requests a reconnect to the websocket
     */
    override fun requestReload() {
        super.requestReload()
        viewModelScope.launch {
            if (!websocketProvider.isConnected.first()) {
                websocketProvider.requestTryReconnect()
            }
        }
    }

    protected abstract suspend fun getMetisContext(): MetisContext

    private val IBasePost.asAffectedPost: MetisModificationService.AffectedPost
        get() = when (this) {
            is AnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is IAnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is StandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
            is IStandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
        }

    protected suspend fun loadConversation(): Conversation? {
        val metisContext = getMetisContext()

        if (metisContext !is MetisContext.Conversation) return null

        return conversationService.getConversation(
            metisContext.courseId,
            metisContext.conversationId,
            accountService.authToken.first(),
            serverConfigurationService.serverUrl.first()
        ).orNull()
    }
}