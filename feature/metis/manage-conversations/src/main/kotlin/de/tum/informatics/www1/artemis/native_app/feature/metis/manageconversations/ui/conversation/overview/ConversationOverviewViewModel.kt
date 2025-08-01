package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState.Success
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.keepSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationsOverviewSection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.util.ConversationCollectionInitUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.util.ConversationOverviewUtils
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisCrudAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToConversationUpdates
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToPostUpdates
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
import kotlinx.coroutines.flow.debounce
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds


private const val TAG = "ConversationOverviewViewModel"

class ConversationOverviewViewModel(
    currentActivityListener: CurrentActivityListener?,
    val courseId: Long,
    private val conversationService: ConversationService,
    private val channelService: ChannelService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val conversationPreferenceService: ConversationPreferenceService,
    websocketProvider: WebsocketProvider,
    networkStatusProvider: NetworkStatusProvider,
    accountDataService: AccountDataService,
    courseService: CourseService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisViewModel(
    courseService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext,
    courseId
) {

    constructor(
        application: Application,
        courseId: Long,
        conversationService: ConversationService,
        channelService: ChannelService,
        serverConfigurationService: ServerConfigurationService,
        accountService: AccountService,
        conversationPreferenceService: ConversationPreferenceService,
        websocketProvider: WebsocketProvider,
        networkStatusProvider: NetworkStatusProvider,
        accountDataService: AccountDataService,
        courseService: CourseService,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ) : this(
        application as? CurrentActivityListener,
        courseId,
        conversationService,
        channelService,
        serverConfigurationService,
        accountService,
        conversationPreferenceService,
        websocketProvider,
        networkStatusProvider,
        accountDataService,
        courseService,
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

    private val _currentFilter =
        MutableStateFlow<ConversationOverviewUtils.ConversationFilter>(ConversationOverviewUtils.ConversationFilter.All)
    val currentFilter: StateFlow<ConversationOverviewUtils.ConversationFilter> = _currentFilter

    private val _scrollPosition = MutableStateFlow(0)
    val scrollPosition: StateFlow<Int> = _scrollPosition

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

    private val postUpdates: Flow<MetisPostDTO> = clientId
        .filterSuccess()
        .flatMapLatest { userId ->
            websocketProvider.subscribeToPostUpdates(courseId, userId)
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
    private val loadedConversations: StateFlow<DataState<List<Conversation>>> =
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReload.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.getConversations(courseId, authToken, serverUrl)
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    /**
     * Conversations of the server updates by the websocket.
     */
    private val updatedConversations: StateFlow<DataState<List<Conversation>>> =
        loadedConversations
            .flatMapLatest { loadedConversationsDataState ->
                when (loadedConversationsDataState) {
                    is Success -> getUpdateConversationsFlow(loadedConversationsDataState.data)

                    is DataState.Loading -> flowOf(DataState.Loading())
                    is DataState.Failure -> flowOf(DataState.Failure(loadedConversationsDataState.throwable))
                }
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.WhileSubscribed())

    private val unresolvedChannels: StateFlow<DataState<List<Conversation>>> =
        loadedConversations.flatMapLatest { conversations ->
            when (conversations) {
                is Success -> {
                    val channelIds = conversations.data.filterIsInstance<ChannelChat>()
                        .filter { channel -> !channel.isAnnouncementChannel && channel.isCourseWide }
                        .map { it.id }

                    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                        channelService.getUnresolvedChannels(
                            channelIds
                        )
                    }
                }
                is DataState.Loading -> flowOf(DataState.Loading())
                is DataState.Failure -> flowOf(DataState.Failure(conversations.throwable))
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    private val recentConversations: StateFlow<DataState<List<Conversation>>> =
        combine(updatedConversations, course) { conversationsDataState, courseDataState ->
            when {
                conversationsDataState is Success && courseDataState is Success -> {
                    Success(conversationsDataState.data.filter { conversation ->
                        ConversationOverviewUtils.isRecent(conversation, courseDataState.data)
                    })
                }
                conversationsDataState is DataState.Loading || courseDataState is DataState.Loading -> {
                    DataState.Loading()
                }
                else -> {
                    DataState.Failure(
                        IllegalStateException("Failed to load recent conversations: $conversationsDataState")
                    )
                }
            }
        }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

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

    private val isFiltering: StateFlow<Boolean> = query
        .map { it.isNotBlank() }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)


    private val conversationsAsCollections: StateFlow<DataState<ConversationCollections>> =
        combine(
            updatedConversations,
            currentPreferences,
            isFiltering
        ) { conversationsDataState, preferences, filterActive ->
            conversationsDataState.bind { conversations ->
                ConversationCollectionInitUtil.fromConversationList(
                    conversations = conversations,
                    preferences = preferences,
                    filterActive = filterActive
                )
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)


    /**
     * Holds the latest conversations we could successfully load.
     */
    private val latestConversations: StateFlow<DataState<ConversationCollections>> =
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

    val conversations: StateFlow<DataState<ConversationCollections>> =
        combine(
            latestConversations,
            unresolvedChannels,
            recentConversations,
            query,
            currentFilter
        ) { latestConversationsDataState, unresolvedChannels, recentConversations, query, filter ->
            latestConversationsDataState.bind { latestConversations ->
                var filteredConversations = latestConversations

                filteredConversations = when (filter) {
                    ConversationOverviewUtils.ConversationFilter.Unread -> filteredConversations.filterUnread()
                    ConversationOverviewUtils.ConversationFilter.Recent -> {
                        recentConversations.orNull()?.let {
                            filteredConversations.filterRecent(it)
                        } ?: filteredConversations
                    }
                    ConversationOverviewUtils.ConversationFilter.Unresolved -> {
                        filteredConversations.filterUnresolved(
                            unresolvedChannels.orNull() ?: emptyList()
                        )
                    }
                    else -> filteredConversations
                }

                if (query.isNotBlank()) {
                    filteredConversations = filteredConversations.filtered(query)
                }
                filteredConversations
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val _isDisplayingErrorDialog = MutableStateFlow(false)
    val isDisplayingErrorDialog: StateFlow<Boolean> = _isDisplayingErrorDialog

    val availableFilters: StateFlow<List<ConversationOverviewUtils.ConversationFilter>> = combine(
        latestConversations,
        recentConversations,
        isAtLeastTutorInCourse
    ) { conversationsDataState, recentConversations, isAtLeastTutorInCourse ->
        val filters = mutableListOf<ConversationOverviewUtils.ConversationFilter>()

        conversationsDataState.bind { conversations ->
            // only tutors should see the unresolved filter
            if (isAtLeastTutorInCourse) filters.add(ConversationOverviewUtils.ConversationFilter.Unresolved)
            if (recentConversations.orNull()?.isNotEmpty() == true) filters.add(
                ConversationOverviewUtils.ConversationFilter.Recent
            )
            if (conversations.hasUnreadMessages()) filters.add(ConversationOverviewUtils.ConversationFilter.Unread)
            if (filters.isNotEmpty()) filters.add(ConversationOverviewUtils.ConversationFilter.All)

            filters.toList().reversed()
        }.orNull() ?: emptyList()
    }.debounce(200)
        .onEach { filters ->
            // if the current filter is not available anymore, we reset it to all
            if (filters.none { it.id == _currentFilter.value.id }) {
                onUpdateFilter(ConversationOverviewUtils.ConversationFilter.All)
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    private fun getUpdateConversationsFlow(loadedConversations: List<Conversation>): Flow<Success<List<Conversation>>> =
        flow {
            val currentConversations =
                loadedConversations.associateBy { it.id }.toMutableMap()

            emit(loadedConversations)

            merge(
                manualConversationUpdates,
                conversationUpdates.map(::ServerSentConversationUpdate),
                postUpdates.map(::ServerSentPostUpdate)
            )
                .collect { update ->
                    when (update) {
                        is MarkMessagesRead -> {
                            val affectedConversation =
                                currentConversations[update.conversationId] ?: return@collect
                            currentConversations[update.conversationId] =
                                affectedConversation.withUnreadMessagesCount(0L)
                        }

                        is ServerSentConversationUpdate -> {
                            val serverConversationUpdate = update.update

                            when (serverConversationUpdate.action) {
                                MetisCrudAction.CREATE, MetisCrudAction.UPDATE -> {
                                    currentConversations[serverConversationUpdate.conversation.id] =
                                        serverConversationUpdate.conversation
                                }

                                MetisCrudAction.DELETE -> {
                                    currentConversations.remove(serverConversationUpdate.conversation.id)
                                }
                            }
                        }

                        is ServerSentPostUpdate -> {
                            val postUpdate = update.update
                            if (postUpdate.action != MetisCrudAction.CREATE) return@collect

                            Log.d(TAG, "Received post creation update: $postUpdate")

                            val conversationId = postUpdate.post.conversation?.id ?: return@collect
                            val isConversationVisible =
                                visibleMetisContexts.value.any { it.isInConversation(conversationId) }
                            // The user is currently looking at the conversation in the UI
                            if (isConversationVisible) {
                                Log.d(
                                    TAG,
                                    "Conversation is visible, not increasing unread messages count"
                                )
                                conversationService.markConversationAsRead(
                                    courseId,
                                    conversationId,
                                    accountService.authToken.first(),
                                    serverConfigurationService.serverUrl.first()
                                )
                                return@collect
                            }

                            val existingConversation =
                                currentConversations[conversationId] ?: return@collect

                            currentConversations[conversationId] =
                                existingConversation.withUnreadMessagesCount(
                                    (existingConversation.unreadMessagesCount ?: 0) + 1
                                )
                        }
                    }

                    emit(currentConversations.values.toList())
                }
        }
            .map(::Success)

    fun onUpdateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun onUpdateFilter(newFilter: ConversationOverviewUtils.ConversationFilter) {
        _currentFilter.value = newFilter
    }

    fun markConversationAsHidden(conversationId: Long, hidden: Boolean): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            // Get current conversation state to check if it's favorite
            val currentConversations = updatedConversations.value.orNull() ?: emptyList()
            val conversation = currentConversations.find { it.id == conversationId }
            
            // If we're hiding a favorite conversation, also unfavorite it
            val shouldUnfavorite = hidden && conversation?.isFavorite == true
            
            val hideResult = conversationService.markConversationAsHidden(
                courseId,
                conversationId,
                hidden,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .onSuccess { isSuccessful ->
                    if (isSuccessful) {
                        requestReload.tryEmit(Unit)
                    }
                }
                .or(false)
            
            // If hiding was successful and we need to unfavorite, do that too
            if (hideResult && shouldUnfavorite) {
                conversationService.markConversationAsFavorite(
                    courseId = courseId,
                    conversationId = conversationId,
                    favorite = false,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
                    .onSuccess { successful ->
                        if (successful) {
                            requestReload.tryEmit(Unit)
                        }
                    }
                    .or(false)
            } else {
                hideResult
            }
        }
    }

    fun markConversationAsMuted(conversationId: Long, muted: Boolean): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            conversationService.markConversationMuted(
                courseId,
                conversationId,
                muted,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .onSuccess { isSuccessful ->
                    if (isSuccessful) {
                        requestReload.tryEmit(Unit)
                    }
                }
                .or(false)
        }
    }

    fun markConversationAsFavorite(conversationId: Long, favorite: Boolean): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            // Get current conversation state to check if it's hidden
            val currentConversations = updatedConversations.value.orNull() ?: emptyList()
            val conversation = currentConversations.find { it.id == conversationId }
            
            // If we're favoriting a hidden conversation, also unhide it
            val shouldUnhide = favorite && conversation?.isHidden == true
            
            val favoriteResult = conversationService.markConversationAsFavorite(
                courseId = courseId,
                conversationId = conversationId,
                favorite = favorite,
                authToken = accountService.authToken.first(),
                serverUrl = serverConfigurationService.serverUrl.first()
            )
                .onSuccess { successful ->
                    if (successful) {
                        requestReload.tryEmit(Unit)
                    }
                }
                .or(false)
            
            // If favoriting was successful and we need to unhide, do that too
            if (favoriteResult && shouldUnhide) {
                conversationService.markConversationAsHidden(
                    courseId,
                    conversationId,
                    hidden = false,
                    accountService.authToken.first(),
                    serverConfigurationService.serverUrl.first()
                )
                    .onSuccess { isSuccessful ->
                        if (isSuccessful) {
                            requestReload.tryEmit(Unit)
                        }
                    }
                    .or(false)
            } else {
                favoriteResult
            }
        }
    }

    fun markAllConversationsAsRead(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            conversationService.markAllConversationsAsRead(
                courseId = courseId,
                authToken = accountService.authToken.first(),
                serverUrl = serverConfigurationService.serverUrl.first()
            )
                .onSuccess { isSuccessful ->
                    if (isSuccessful) {
                        requestReload.tryEmit(Unit)
                    }
                }
                .onFailure {
                    _isDisplayingErrorDialog.value = true
                }
                .or(false)
        }
    }

    fun dismissErrorDialog() {
        _isDisplayingErrorDialog.value = false
    }


    fun setConversationMessagesRead(conversationId: Long) {
        viewModelScope.launch(coroutineContext) {
            manualConversationUpdates.emit(MarkMessagesRead(conversationId))
        }
    }

    fun toggleSectionExpanded(section: ConversationsOverviewSection) {
        viewModelScope.launch(coroutineContext) {
            val currentPreferences = currentPreferences.first()

            conversationPreferenceService.updatePreferences(
                serverUrl = serverConfigurationService.serverUrl.first(),
                courseId = courseId,
                preferences = currentPreferences.toggle(section)
            )
        }
    }

    fun saveScrollPosition(position: Int) {
        _scrollPosition.value = position
    }
}
