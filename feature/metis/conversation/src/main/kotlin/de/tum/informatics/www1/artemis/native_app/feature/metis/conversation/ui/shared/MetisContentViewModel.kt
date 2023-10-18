package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.holdLatestLoaded
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToConversationUpdates
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.MetisViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

/**
 * Common viewModel for metis viewModels that display live metis content.
 * Live metis content is content that is being permanently updated by websockets.
 */
internal abstract class MetisContentViewModel(
    initialMetisContext: MetisContext,
    private val websocketProvider: WebsocketProvider,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService,
    private val coroutineContext: CoroutineContext
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext
) {

    protected val metisContext = MutableStateFlow(initialMetisContext)
    val currentMetisContext: StateFlow<MetisContext> = metisContext

    val hasModerationRights: StateFlow<Boolean> = metisContext.flatMapLatest { metisContext ->
        when (metisContext) {
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
                                authToken = authToken,
                                serverUrl = serverUrl
                            )
                            .bind { it.hasModerationRights }
                    }
                        .map { it.orElse(false) }
                }
            }

            else -> flowOf(false)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    /**
     * Emits true if the data may be outdated. Listens to the connection state of the websocket
     * If a connection was established and is broken, then the data may be corrupted. A reload resets this
     */
    val isDataOutdated: StateFlow<Boolean> = merge(
        onRequestReload,
        metisContext.map { }
    )
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
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val conversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        metisContext,
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onReloadRequestAndWebsocketReconnect.onStart { emit(Unit) }
    ) { metisContext, serverUrl, authToken, _ ->
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
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val latestUpdatedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        metisContext,
        metisContext.flatMapLatest { conversation.holdLatestLoaded() },
        clientId.filterSuccess()
    ) { metisContext, conversationDataState, clientId ->
        websocketProvider.subscribeToConversationUpdates(clientId, metisContext.courseId)
            .filter { it.crudAction == MetisPostAction.UPDATE }
            .map<ConversationWebsocketDto, DataState<Conversation>> { DataState.Success(it.conversation) }
            .onStart { emit(conversationDataState) }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    /**
     * Handles a reaction click. If the client has already reacted, it deletes the reaction.
     * Otherwise it creates a reaction with the same emoji id.
     */
    fun createOrDeleteReaction(
        post: IBasePost,
        emojiId: String,
        create: Boolean
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            if (create) {
                createReactionImpl(emojiId, post.asAffectedPost)
            } else {
                val clientId = clientId.value.orNull()
                    ?: return@async MetisModificationFailure.DELETE_REACTION

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
            context = metisContext.value,
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
            context = metisContext.value,
            reactionId = reactionId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )
            .onFailure {
                println("Could not delete $it")
            }
            .or(false)

        return if (success) null else MetisModificationFailure.DELETE_REACTION
    }

    protected suspend fun createStandalonePostImpl(post: StandalonePost): MetisModificationFailure? {
        val metisContext = metisContext.value
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
            context = metisContext.value,
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return response.bind<MetisModificationFailure?> { null }
            .or(MetisModificationFailure.CREATE_POST)
    }

    fun deletePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            metisModificationService.deletePost(
                metisContext.value,
                post.asAffectedPost,
                serverConfigurationService.serverUrl.first(),
                accountService.authToken.first()
            )
                .bind { if (it) null else MetisModificationFailure.DELETE_POST }
                .or(MetisModificationFailure.DELETE_POST)
        }
    }

    fun editPost(post: PostPojo, newText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val newPost = StandalonePost(
                post = post.copy(content = newText),
                conversation = conversation
            )

            metisModificationService.updateStandalonePost(
                context = metisContext.value,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun editAnswerPost(
        parentPost: PostPojo,
        post: AnswerPostPojo,
        newText: String
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val serializedParentPost = StandalonePost(parentPost, conversation)
            val newPost = AnswerPost(post, serializedParentPost).copy(content = newText)

            metisModificationService.updateAnswerPost(
                context = metisContext.value,
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
        viewModelScope.launch(coroutineContext) {
            if (!websocketProvider.isConnected.first()) {
                websocketProvider.requestTryReconnect()
            }
        }
    }

    private val IBasePost.asAffectedPost: MetisModificationService.AffectedPost
        get() = when (this) {
            is AnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is IAnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is StandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
            is IStandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
        }

    protected suspend fun loadConversation(): Conversation? {
        val metisContext = metisContext.value

        if (metisContext !is MetisContext.Conversation) return null

        return conversationService.getConversation(
            metisContext.courseId,
            metisContext.conversationId,
            accountService.authToken.first(),
            serverConfigurationService.serverUrl.first()
        ).orNull()
    }

    fun updateMetisContext(newMetisContext: MetisContext) {
        metisContext.value = newMetisContext
    }
}