package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.Reaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val serverDataService: ServerDataService,
    private val networkStatusProvider: NetworkStatusProvider
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

    /**
     * Handles a reaction click. If the client has already reacted, it deletes the reaction.
     * Otherwise it creates a reaction with the same emoji id.
     */
    fun onClickReaction(
        emojiId: String,
        post: MetisModificationService.AffectedPost,
        presentReactions: List<Post.Reaction>,
        response: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            onClickReactionImpl(emojiId, post, presentReactions, response)
        }
    }

    protected suspend fun onClickReactionImpl(
        emojiId: String,
        post: MetisModificationService.AffectedPost,
        presentReactions: List<Post.Reaction>,
        response: (MetisModificationFailure?) -> Unit
    ) {
        when (val clientId = clientId.first()) {
            is DataState.Success -> {
                val userId = clientId.data

                val existingReaction = presentReactions
                    .filter { it.emojiId == emojiId }
                    .firstOrNull { it.authorId == userId }

                if (existingReaction != null) {
                    deleteReaction(existingReaction.id, response)
                } else {
                    createReactionImpl(emojiId, post, response)
                }
            }

            else -> {
                response(MetisModificationFailure.CREATE_REACTION)
            }
        }
    }

    fun createReaction(
        emojiId: String,
        post: MetisModificationService.AffectedPost,
        response: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            createReactionImpl(emojiId, post, response)
        }
    }

    private suspend fun deleteReaction(
        reactionId: Long,
        response: (MetisModificationFailure?) -> Unit
    ) {
        metisModificationService.deleteReaction(
            context = getMetisContext(),
            reactionId = reactionId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    response(MetisModificationFailure.CREATE_REACTION)
                    return
                }
            }
        )
    }

    protected suspend fun createReactionImpl(
        emojiId: String,
        post: MetisModificationService.AffectedPost,
        response: (MetisModificationFailure?) -> Unit
    ) {
        val networkResponse: NetworkResponse<Reaction> = metisModificationService.createReaction(
            context = getMetisContext(),
            post = post,
            emojiId = emojiId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    response(MetisModificationFailure.CREATE_REACTION)
                    return
                }
            }
        )

        response(
            when (networkResponse) {
                is NetworkResponse.Failure -> MetisModificationFailure.CREATE_REACTION
                is NetworkResponse.Response -> null
            }
        )
    }

    /**
     * @return the client side post id on success
     */
    protected suspend fun createStandalonePostImpl(post: StandalonePost): MetisModificationResponse<String> {
        val failure = MetisModificationResponse.Failure<String>(
            MetisModificationFailure.CREATE_POST
        )

        val metisContext = getMetisContext()
        val response = metisModificationService.createPost(
            context = metisContext,
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    return failure
                }
            }
        )

        return when (response) {
            is NetworkResponse.Failure -> failure
            is NetworkResponse.Response -> {
                val clientSidePostId = metisStorageService.insertLiveCreatedPost(
                    serverConfigurationService.host.first(),
                    metisContext,
                    response.data
                )

                if (clientSidePostId == null) {
                    failure
                } else {
                    MetisModificationResponse.Response(clientSidePostId)
                }
            }
        }
    }

    protected suspend fun createAnswerPostImpl(
        post: AnswerPost,
        onResponse: (MetisModificationFailure?) -> Unit
    ) {
        val response = metisModificationService.createAnswerPost(
            context = getMetisContext(),
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    onResponse(MetisModificationFailure.CREATE_POST)
                    return
                }
            }
        )

        when (response) {
            is NetworkResponse.Failure -> {
                onResponse(MetisModificationFailure.CREATE_POST)
            }

            is NetworkResponse.Response -> {
                onResponse(null)
            }
        }
    }

    protected abstract suspend fun getMetisContext(): MetisContext

    open fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}