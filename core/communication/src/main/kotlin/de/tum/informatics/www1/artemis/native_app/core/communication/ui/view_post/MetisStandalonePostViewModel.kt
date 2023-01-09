package de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class MetisStandalonePostViewModel(
    private val clientSidePostId: String,
    private val metisContext: MetisContext,
    subscribeToLiveUpdateService: Boolean,
    private val websocketProvider: WebsocketProvider,
    private val metisStorageService: MetisStorageService,
    metisContextManager: MetisContextManager,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverDataService: ServerDataService
) : MetisViewModel(metisService, metisStorageService, serverConfigurationService, accountService, serverDataService, networkStatusProvider) {

    private val collectMetisUpdates: Flow<MetisContextManager.CurrentDataAction> =
        metisContextManager.getContextDataActionFlow(metisContext)
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val post: Flow<Post?> =
        metisStorageService
            .getStandalonePost(clientSidePostId)
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val isDataOutdated: Flow<Boolean> = collectMetisUpdates.map {
        when (it) {
            MetisContextManager.CurrentDataAction.Keep -> false
            MetisContextManager.CurrentDataAction.Outdated -> true
            MetisContextManager.CurrentDataAction.Refresh -> false
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
                collectMetisUpdates
                    .filter { it == MetisContextManager.CurrentDataAction.Refresh }
                    .collect {
                        val serverSidePostId = post.filterNotNull().first().serverPostId
                        val serverUrl = serverConfigurationService.serverUrl.first()
                        val host = serverConfigurationService.host.first()
                        val authToken = when (val authData =
                            accountService.authenticationData.first()) {
                            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                            AccountService.AuthenticationData.NotLoggedIn -> return@collect
                        }

                        retryOnInternet(
                            networkStatusProvider.currentNetworkStatus
                        ) {
                            metisService.getPost(
                                metisContext,
                                serverSidePostId,
                                serverUrl,
                                authToken
                            )
                        }.collect { dataState ->
                            if (dataState is DataState.Success) {
                                metisStorageService.updatePost(
                                    host,
                                    metisContext,
                                    dataState.data
                                )
                            }
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
                post = MetisService.AffectedPost.Answer(postId = serverSidePostId),
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
                post = MetisService.AffectedPost.Answer(postId = serverSidePostId),
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
                    id = metisStorageService.getServerSidePostId(
                        serverConfigurationService.host.first(),
                        clientSidePostId
                    )
                )
            )

            createAnswerPostImpl(replyPost, onResponse)
        }
    }

    override suspend fun getMetisContext(): MetisContext = metisContext
}