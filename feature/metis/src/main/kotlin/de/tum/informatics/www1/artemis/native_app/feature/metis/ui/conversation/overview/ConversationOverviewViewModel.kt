package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState.Success
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.keepSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationWebsocketDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.subscribeToConversationUpdates
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlin.time.Duration.Companion.seconds

class ConversationOverviewViewModel(
    private val courseId: Long,
    private val conversationService: ConversationService,
    websocketProvider: WebsocketProvider,
    networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverDataService: ServerDataService
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider,
    websocketProvider
) {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val conversationUpdates: Flow<ConversationWebsocketDTO> = clientId
        .filterSuccess()
        .flatMapLatest { userId ->
            websocketProvider.subscribeToConversationUpdates(userId, courseId)
        }
        .shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
            replay = 0
        )

    /**
     * Conversations as loaded from the server.
     */
    private val loadedConversations: StateFlow<DataState<List<Conversation>>> =
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            onRequestReload.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.getConversations(courseId, authToken, serverUrl)
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Conversations of the server updates by the websocket.
     */
    private val updatedConversations: StateFlow<DataState<List<Conversation>>> = loadedConversations
        .flatMapLatest { loadedConversationsDataState ->
            when (loadedConversationsDataState) {
                is Success -> getUpdateConversationsFlow(loadedConversationsDataState.data)

                is DataState.Loading -> flowOf(DataState.Loading())
                is DataState.Failure -> flowOf(DataState.Failure(loadedConversationsDataState.throwable))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    private fun getUpdateConversationsFlow(loadedConversations: List<Conversation>): Flow<Success<List<Conversation>>> =
        flow {
            val currentConversations =
                loadedConversations.associateBy { it.id }.toMutableMap()

            emit(loadedConversations)

            conversationUpdates.collect { update ->
                when (update.crudAction) {
                    MetisPostAction.CREATE, MetisPostAction.UPDATE -> {
                        currentConversations[update.conversation.id] = update.conversation
                    }

                    MetisPostAction.NEW_MESSAGE -> {
                        val existingConversation = currentConversations[update.conversation.id]
                        if (existingConversation != null) {
                            currentConversations[update.conversation.id] =
                                existingConversation.withUnreadMessagesCount(
                                    (existingConversation.unreadMessagesCount ?: 0) + 1
                                )
                        }
                    }

                    MetisPostAction.DELETE -> {
                        currentConversations.remove(update.conversation.id)
                    }
                }

                emit(currentConversations.values.toList())
            }
        }
            .map(::Success)

    val isConnected: StateFlow<Boolean> =
        websocketProvider.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val conversationsAsCollections: StateFlow<DataState<ConversationCollection>> =
        updatedConversations
            .map { conversationsDataState ->
                conversationsDataState.bind { conversations ->
                    ConversationCollection(
                        channels = conversations.filterNotHiddenNorFavourite(),
                        groupChats = conversations.filterNotHiddenNorFavourite(),
                        directChats = conversations.filterNotHiddenNorFavourite(),
                        favorites = conversations.filter { it.isFavorite },
                        hidden = conversations.filter { it.isHidden }
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Holds the latest conversations we could successfully load.
     */
    private val latestConversations: StateFlow<DataState<ConversationCollection>> =
        conversationsAsCollections
            .transformWhile { conversationsAsCollections ->
                emit(conversationsAsCollections)
                conversationsAsCollections !is Success
            }
            // After we have received our first success, we only take winners
            .onCompletion {
                emitAll(
                    conversationsAsCollections.keepSuccess()
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    val conversations: StateFlow<DataState<ConversationCollection>> =
        combine(latestConversations, query) { latestConversationsDataState, query ->
            if (query.isBlank()) {
                latestConversationsDataState
            } else {
                latestConversationsDataState.bind { latestConversations ->
                    latestConversations.filtered(query)
                }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    fun onUpdateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun markConversationAsHidden(conversationId: Long, hidden: Boolean): Deferred<Boolean> {
        return viewModelScope.async {
            conversationService.markConversationAsHidden(
                courseId,
                conversationId,
                hidden,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .onSuccess {
                    if (it) {
                        onRequestReload.tryEmit(Unit)
                    }
                }
                .or(false)
        }
    }

    fun markConversationAsFavorite(conversationId: Long, favorite: Boolean): Deferred<Boolean> {
        return viewModelScope.async {
            conversationService.markConversationAsFavorite(
                courseId,
                conversationId,
                favorite,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .onSuccess {
                    if (it) {
                        onRequestReload.tryEmit(Unit)
                    }
                }
                .or(false)
        }
    }

    private inline fun <reified T : Conversation> List<*>.filterNotHiddenNorFavourite(): List<T> {
        return filterIsInstance<T>()
            .filter { !it.isHidden && !it.isFavorite }
    }
}