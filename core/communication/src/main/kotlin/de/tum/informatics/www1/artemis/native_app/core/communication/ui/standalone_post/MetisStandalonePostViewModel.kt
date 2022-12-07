package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MetisStandalonePostViewModel(
    private val clientSidePostId: String,
    subscribeToLiveUpdateService: Boolean,
    private val metisStorageService: MetisStorageService,
    metisContextManager: MetisContextManager,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService
) : MetisViewModel(metisService, serverConfigurationService, accountService) {

    val metisContext: Flow<MetisContext> = flow {
        emit(metisStorageService.getStandalonePostMetisContext(clientSidePostId))
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val collectMetisUpdates: Flow<MetisContextManager.CurrentDataAction> =
        metisContext.flatMapLatest {
            metisContextManager.getContextDataActionFlow(it)
        }
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
                combine(
                    serverConfigurationService.host,
                    metisContext
                ) { a, b -> a to b }
                    .collectLatest { (host, context) ->
                        metisContextManager.updatePosts(host, context)
                    }
            }

            viewModelScope.launch {
                metisContext.collectLatest { currentMetisContext ->
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
                                    currentMetisContext,
                                    serverSidePostId,
                                    serverUrl,
                                    authToken
                                )
                            }.collect { dataState ->
                                if (dataState is DataState.Success) {
                                    metisStorageService.updatePost(
                                        host,
                                        currentMetisContext,
                                        dataState.data
                                    )
                                }
                            }
                        }
                }
            }
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

    override suspend fun getMetisContext(): MetisContext = metisContext.first()
}