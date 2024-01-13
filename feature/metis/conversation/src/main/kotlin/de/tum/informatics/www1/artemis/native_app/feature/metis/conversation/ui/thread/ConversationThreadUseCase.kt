package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread

import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel for the standalone view of communication posts. Handles loading of the singular post by the given post id.
 */
internal class ConversationThreadUseCase(
    metisContext: MetisContext,
    postId: Flow<StandalonePostId>,
    onRequestReload: MutableSharedFlow<Unit>,
    viewModelScope: CoroutineScope,
    private val metisStorageService: MetisStorageService,
    private val metisService: MetisService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    companion object {
        fun Flow<PostPojo?>.asDataStateFlow(): Flow<DataState<PostPojo>> = map { post ->
            if (post != null) {
                DataState.Success(post)
            } else DataState.Failure(RuntimeException("Post not found"))
        }
    }

    /**
     * The post data state flow as loading from the server.
     */
    val post: StateFlow<DataState<PostPojo>> = postId.flatMapLatest { postId ->
        when (postId) {
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
                    handleServerLoadedStandalonePost(metisContext, standalonePostDataState)
                }
                .onStart { emit(DataState.Loading()) }
        }
    }
        .map { dataState -> dataState.bind { it } } // Type check adaption
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

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
}
