package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.Reaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService
) : ViewModel() {

    /**
     * Handles a reaction click. If the client has already reacted, it deletes the reaction.
     * Otherwise it creates a reaction with the same emoji id.
     */
    fun onClickReaction(
        emojiId: String,
        post: MetisService.AffectedPost,
        presentReactions: List<Post.Reaction>,
        response: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            onClickReactionImpl(emojiId, post, presentReactions, response)
        }
    }

    protected suspend fun onClickReactionImpl(
        emojiId: String,
        post: MetisService.AffectedPost,
        presentReactions: List<Post.Reaction>,
        response: (MetisModificationFailure?) -> Unit
    ) {
        when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> {
                val userId = authData.account.bind { it.id }.orElse(null) ?: return

                val existingReaction = presentReactions
                    .filter { it.emojiId == emojiId }
                    .firstOrNull { it.authorId == userId }

                if (existingReaction != null) {
                    deleteReaction(existingReaction.id, response)
                } else {
                    createReactionImpl(emojiId, post, response)
                }
            }

            AccountService.AuthenticationData.NotLoggedIn -> {
                // Do nothing.
            }
        }
    }

    fun createReaction(
        emojiId: String,
        post: MetisService.AffectedPost,
        response: (MetisModificationFailure?) -> Unit
    ) {
        viewModelScope.launch {
            createReactionImpl(emojiId, post, response)
        }
    }

    private suspend fun deleteReaction(
        reactionId: Int,
        response: (MetisModificationFailure?) -> Unit
    ) {
        metisService.deleteReaction(
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
        post: MetisService.AffectedPost,
        response: (MetisModificationFailure?) -> Unit
    ) {
        val networkResponse: NetworkResponse<Reaction> = metisService.createReaction(
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
     * @param onResponse contains the client side post id on success
     */
    protected fun createStandalonePost(
        post: StandalonePost,
        onResponse: (MetisModificationResponse<String>) -> Unit
    ): Job {
        val onFailure = {
            onResponse(
                MetisModificationResponse.Failure(MetisModificationFailure.CREATE_POST)
            )
        }

        return viewModelScope.launch {
            val metisContext = getMetisContext()
            val response = metisService.createPost(
                context = metisContext,
                post = post,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = when (val authData = accountService.authenticationData.first()) {
                    is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        onFailure()
                        return@launch
                    }
                }
            )

            when (response) {
                is NetworkResponse.Failure -> onFailure()
                is NetworkResponse.Response -> {
                    val clientSidePostId = metisStorageService.insertLiveCreatedPost(
                        serverConfigurationService.host.first(),
                        metisContext,
                        response.data
                    )

                    if (clientSidePostId == null) {
                        onFailure()
                    } else {
                        onResponse(
                            MetisModificationResponse.Response(clientSidePostId)
                        )
                    }
                }
            }
        }
    }

    protected suspend fun createAnswerPostImpl(
        post: AnswerPost,
        onResponse: (MetisModificationFailure?) -> Unit
    ) {
        val response = metisService.createAnswerPost(
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
}