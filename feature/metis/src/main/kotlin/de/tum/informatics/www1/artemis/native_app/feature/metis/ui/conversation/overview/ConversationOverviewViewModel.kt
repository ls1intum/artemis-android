package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState.Success
import de.tum.informatics.www1.artemis.native_app.core.data.keepSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationWebsocketDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
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
    conversationService: ConversationService,
    websocketProvider: WebsocketProvider,
    networkStatusProvider: NetworkStatusProvider,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    serverDataService: ServerDataService
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val userId: StateFlow<DataState<Long>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            serverDataService
                .getAccountData(serverUrl, authToken)
                .bind { it.id }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    private val conversationUpdates: Flow<ConversationWebsocketDTO> = userId
        .flatMapLatest { userId ->
            val topic = "/user/topic/metis/courses/$courseId/conversations/user/$userId"

            websocketProvider.subscribeMessage(topic, ConversationWebsocketDTO.serializer())
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
                    MetisPostAction.CREATE, MetisPostAction.UPDATE, MetisPostAction.NEW_MESSAGE -> {
                        currentConversations[update.conversation.id] = update.conversation
                    }

                    MetisPostAction.DELETE -> {
                        currentConversations.remove(update.conversation.id)
                    }
                }

                emit(currentConversations.values.toList())
            }
        }
            .map(::Success)

    val isReloadingConversations: StateFlow<Boolean> = updatedConversations
        .map { conversations ->
            conversations !is Success
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

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

    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }

    fun onUpdateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun markConversationAsHidden(conversationId: Long, hidden: Boolean) {

    }

    fun markConversationAsFavorite(conversationId: Long, favorite: Boolean) {

    }

    private inline fun <reified T : Conversation> List<*>.filterNotHiddenNorFavourite(): List<T> {
        return filterIsInstance<T>()
            .filter { !it.isHidden && !it.isFavorite }
    }
}