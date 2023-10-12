package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import android.app.Activity
import android.app.Application
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState.Success
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.keepSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToConversationUpdates
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextReporter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

class ConversationOverviewViewModel(
    currentActivityListener: CurrentActivityListener?,
    private val courseId: Long,
    private val conversationService: ConversationService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val conversationPreferenceService: ConversationPreferenceService,
    websocketProvider: WebsocketProvider,
    networkStatusProvider: NetworkStatusProvider,
    accountDataService: AccountDataService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext
) {

    constructor(
        application: Application,
        courseId: Long,
        conversationService: ConversationService,
        serverConfigurationService: ServerConfigurationService,
        accountService: AccountService,
        conversationPreferenceService: ConversationPreferenceService,
        websocketProvider: WebsocketProvider,
        networkStatusProvider: NetworkStatusProvider,
        accountDataService: AccountDataService,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ) : this(
        application as? CurrentActivityListener,
        courseId,
        conversationService,
        serverConfigurationService,
        accountService,
        conversationPreferenceService,
        websocketProvider,
        networkStatusProvider,
        accountDataService,
        coroutineContext
    )

    private val currentActivity: Flow<Activity?> =
        currentActivityListener?.currentActivity ?: flowOf(null)

    /**
     * The metis contexts that are currently visible. Used to check if we have to increase the unread messages count on a conversation.
     */
    private val visibleMetisContexts: StateFlow<List<VisibleMetisContext>> = currentActivity
        .filterIsInstance<VisibleMetisContextReporter?>()
        .flatMapLatest { visibleMetisContextReporter ->
            visibleMetisContextReporter?.visibleMetisContexts ?: flowOf(emptyList())
        }
        .flowOn(coroutineContext)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val conversationUpdates: Flow<ConversationWebsocketDto> = clientId
        .filterSuccess()
        .flatMapLatest { userId ->
            websocketProvider.subscribeToConversationUpdates(userId, courseId)
        }
        .flowOn(coroutineContext)
        .shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
            replay = 0
        )

    private val manualConversationUpdates = MutableSharedFlow<MarkMessagesRead>()

    /**
     * Conversations as loaded from the server.
     */
    private val loadedConversations: StateFlow<DataState<List<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>>> =
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            onRequestReload.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.getConversations(courseId, authToken, serverUrl)
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    /**
     * Conversations of the server updates by the websocket.
     */
    private val updatedConversations: StateFlow<DataState<List<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>>> =
        loadedConversations
            .flatMapLatest { loadedConversationsDataState ->
                when (loadedConversationsDataState) {
                    is Success -> getUpdateConversationsFlow(loadedConversationsDataState.data)

                    is DataState.Loading -> flowOf(DataState.Loading())
                    is DataState.Failure -> flowOf(DataState.Failure(loadedConversationsDataState.throwable))
                }
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.WhileSubscribed())

    val isConnected: StateFlow<Boolean> =
        websocketProvider
            .isConnected
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val currentPreferences: Flow<ConversationPreferenceService.Preferences> =
        serverConfigurationService
            .serverUrl
            .flatMapLatest { serverUrl ->
                conversationPreferenceService.getPreferences(serverUrl, courseId)
            }
            .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val conversationsAsCollections: StateFlow<DataState<de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections>> =
        combine(
            updatedConversations,
            currentPreferences
        ) { conversationsDataState, preferences ->
            conversationsDataState.bind { conversations ->
                de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections(
                    channels = conversations.filterNotHiddenNorFavourite<ChannelChat>()
                        .asCollection(preferences.channelsExpanded),
                    groupChats = conversations.filterNotHiddenNorFavourite<GroupChat>()
                        .asCollection(preferences.groupChatsExpanded),
                    directChats = conversations.filterNotHiddenNorFavourite<OneToOneChat>()
                        .asCollection(preferences.personalConversationsExpanded),
                    favorites = conversations.filter { it.isFavorite }
                        .asCollection(preferences.favouritesExpanded),
                    hidden = conversations.filter { it.isHidden }
                        .asCollection(preferences.hiddenExpanded)
                )
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    /**
     * Holds the latest conversations we could successfully load.
     */
    private val latestConversations: StateFlow<DataState<de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections>> =
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
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val conversations: StateFlow<DataState<de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections>> =
        combine(latestConversations, query) { latestConversationsDataState, query ->
            if (query.isBlank()) {
                latestConversationsDataState
            } else {
                latestConversationsDataState.bind { latestConversations ->
                    latestConversations.filtered(query)
                }
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private fun getUpdateConversationsFlow(loadedConversations: List<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>): Flow<Success<List<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation>>> =
        flow {
            val currentConversations =
                loadedConversations.associateBy { it.id }.toMutableMap()

            emit(loadedConversations)

            merge(
                conversationUpdates.map(::ServerSentConversationUpdate),
                manualConversationUpdates
            )
                .collect { update ->
                    when (update) {
                        is MarkMessagesRead -> {
                            val affectedConversation = currentConversations[update.conversationId]
                            if (affectedConversation != null) {
                                currentConversations[update.conversationId] =
                                    affectedConversation.withUnreadMessagesCount(0L)
                            }
                        }

                        is ServerSentConversationUpdate -> {
                            val serverSentUpdate = update.update

                            when (serverSentUpdate.crudAction) {
                                MetisPostAction.CREATE, MetisPostAction.UPDATE -> {
                                    currentConversations[serverSentUpdate.conversation.id] =
                                        serverSentUpdate.conversation
                                }

                                MetisPostAction.NEW_MESSAGE -> {
                                    val isMetisContextVisible =
                                        visibleMetisContexts.value.any { visibleMetisContext ->
                                            val metisContext = visibleMetisContext.metisContext

                                            metisContext is MetisContext.Conversation && metisContext.conversationId == serverSentUpdate.conversation.id
                                        }

                                    val existingConversation =
                                        currentConversations[serverSentUpdate.conversation.id]
                                    if (existingConversation != null && !isMetisContextVisible) {
                                        currentConversations[serverSentUpdate.conversation.id] =
                                            existingConversation.withUnreadMessagesCount(
                                                (existingConversation.unreadMessagesCount ?: 0) + 1
                                            )
                                    }
                                }

                                MetisPostAction.DELETE -> {
                                    currentConversations.remove(serverSentUpdate.conversation.id)
                                }
                            }
                        }
                    }

                    emit(currentConversations.values.toList())
                }
        }
            .map(::Success)

    fun onUpdateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun markConversationAsHidden(conversationId: Long, hidden: Boolean): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            conversationService.markConversationAsHidden(
                courseId,
                conversationId,
                hidden,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .onSuccess { isSuccessful ->
                    if (isSuccessful) {
                        onRequestReload.tryEmit(Unit)
                    }
                }
                .or(false)
        }
    }

    fun markConversationAsFavorite(conversationId: Long, favorite: Boolean): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            conversationService.markConversationAsFavorite(
                courseId = courseId,
                conversationId = conversationId,
                favorite = favorite,
                authToken = accountService.authToken.first(),
                serverUrl = serverConfigurationService.serverUrl.first()
            )
                .onSuccess { successful ->
                    if (successful) {
                        onRequestReload.tryEmit(Unit)
                    }
                }
                .or(false)
        }
    }

    fun setConversationMessagesRead(conversationId: Long) {
        viewModelScope.launch(coroutineContext) {
            manualConversationUpdates.emit(MarkMessagesRead(conversationId))
        }
    }

    fun toggleFavoritesExpanded() {
        expandOrCollapseSection { copy(favouritesExpanded = !favouritesExpanded) }
    }

    fun toggleChannelsExpanded() {
        expandOrCollapseSection { copy(channelsExpanded = !channelsExpanded) }
    }

    fun toggleGroupChatsExpanded() {
        expandOrCollapseSection { copy(groupChatsExpanded = !groupChatsExpanded) }
    }

    fun togglePersonalConversationsExpanded() {
        expandOrCollapseSection { copy(personalConversationsExpanded = !personalConversationsExpanded) }
    }

    fun toggleHiddenExpanded() {
        expandOrCollapseSection { copy(hiddenExpanded = !hiddenExpanded) }
    }

    private fun expandOrCollapseSection(update: ConversationPreferenceService.Preferences.() -> ConversationPreferenceService.Preferences) {
        viewModelScope.launch(coroutineContext) {
            conversationPreferenceService.updatePreferences(
                serverUrl = serverConfigurationService.serverUrl.first(),
                courseId = courseId,
                preferences = update(currentPreferences.first())
            )
        }
    }

    private inline fun <reified T : de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation> List<*>.filterNotHiddenNorFavourite(): List<T> {
        return filterIsInstance<T>()
            .filter { !it.isHidden && !it.isFavorite }
    }

    private fun <T : de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation> List<T>.asCollection(
        isExpanded: Boolean
    ) =
        de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections.ConversationCollection(
            this,
            isExpanded
        )
}