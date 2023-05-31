package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.AnswerPostDb
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val serverDataService: ServerDataService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService
) : ViewModel() {

    protected val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val clientId: StateFlow<DataState<Long>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternetIndefinetly(
            networkStatusProvider.currentNetworkStatus
        ) {
            serverDataService.getAccountData(serverUrl, authToken).bind { it.id }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    val clientIdOrDefault: StateFlow<Long> = clientId
        .map { it.orElse(0L) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

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

    protected suspend fun createReactionImpl(
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

    protected abstract suspend fun getMetisContext(): MetisContext

    open fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }

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
