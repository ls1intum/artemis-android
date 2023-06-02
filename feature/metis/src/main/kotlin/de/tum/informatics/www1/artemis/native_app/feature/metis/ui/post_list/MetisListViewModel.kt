package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl.MetisRemoteMediator
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisContentViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.StandalonePostId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.milliseconds

internal class MetisListViewModel(
    val metisContext: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val metisContextManager: MetisContextManager,
    metisModificationService: MetisModificationService,
    accountService: AccountService,
    websocketProvider: WebsocketProvider,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider,
    conversationService: ConversationService
) : MetisContentViewModel(
    metisContext,
    websocketProvider,
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider,
    conversationService
) {

    private val _filter = MutableStateFlow<List<MetisFilter>>(emptyList())
    val filter: StateFlow<List<MetisFilter>> = _filter

    private val _query = MutableStateFlow<String?>(null)
    val query: StateFlow<String> = _query
        .map(String?::orEmpty)
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val delayedQuery = _query.transformLatest { query ->
        /*
         * Do not search every time the user enters a new character.
         * Wait for 300 ms of the user not entering a char to search
         */
        if (query != null) {
            delay(300.milliseconds)
        }
        emit(query)
    }

    private val _sortingStrategy = MutableStateFlow(MetisSortingStrategy.DATE_DESCENDING)
    val sortingStrategy: StateFlow<MetisSortingStrategy> = _sortingStrategy

    private val _courseWideContext = MutableStateFlow<CourseWideContext?>(null)
    val courseWideContext: StateFlow<CourseWideContext?> = _courseWideContext

    private val standaloneMetisContext: Flow<StandalonePostsContext> = combine(
        _filter,
        delayedQuery,
        _sortingStrategy,
        _courseWideContext
    ) { filter, query, sortingStrategy, courseWideContext ->
        StandalonePostsContext(
            metisContext = metisContext,
            filter = filter,
            query = query,
            sortingStrategy = sortingStrategy,
            courseWideContext = courseWideContext
        )
    }

    private val pagingDataInput: Flow<PagingDataInput> = combine(
        accountService.authenticationData,
        serverConfigurationService.serverUrl,
        serverConfigurationService.host,
        standaloneMetisContext,
        ::PagingDataInput
    )

    private data class PagingDataInput(
        val authenticationData: AccountService.AuthenticationData,
        val serverUrl: String,
        val host: String,
        val standalonePostsContext: StandalonePostsContext
    )

    @OptIn(ExperimentalPagingApi::class)
    val postPagingData: Flow<PagingData<ChatListItem>> =
        flatMapLatest(
            pagingDataInput,
            accountService.authToken,
            clientId.map { it.orElse(0L) },
            onRequestReload.onStart { emit(Unit) }
        ) { pagingDataInput, authToken, clientId, _ ->
            Pager(
                config = PagingConfig(
                    pageSize = 20
                ),
                remoteMediator = MetisRemoteMediator(
                    context = pagingDataInput.standalonePostsContext,
                    metisService = metisService,
                    metisStorageService = metisStorageService,
                    authToken = authToken,
                    serverUrl = pagingDataInput.serverUrl,
                    host = pagingDataInput.host,
                    performInitialRefresh = true
                ),
                pagingSourceFactory = {
                    metisStorageService.getStoredPosts(
                        serverId = pagingDataInput.host,
                        clientId = clientId,
                        filter = pagingDataInput.standalonePostsContext.filter,
                        sortingStrategy = pagingDataInput.standalonePostsContext.sortingStrategy,
                        query = pagingDataInput.standalonePostsContext.query,
                        metisContext = metisContext
                    )
                }
            )
                .flow
                .cachedIn(viewModelScope)
                .map { pagingList -> pagingList.map(ChatListItem::PostChatListItem) }
                .map(::insertDateSeparators)
        }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    init {
        viewModelScope.launch {
            serverConfigurationService.host.collectLatest { host ->
                metisContextManager.updatePosts(host, metisContext)
            }
        }
    }

    fun createPost(postText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.CREATE_POST

            val post = StandalonePost(
                id = null,
                title = null,
                tags = null,
                content = postText,
                conversation = conversation,
                creationDate = Clock.System.now(),
                displayPriority = DisplayPriority.NONE
            )

            createStandalonePostImpl(post)
        }
    }

    fun addMetisFilter(metisFilter: MetisFilter) {
        _filter.value = _filter.value + metisFilter
    }

    fun removeMetisFilter(metisFilter: MetisFilter) {
        _filter.value = _filter.value.filterNot { it == metisFilter }
    }

    fun updateCourseWideContext(new: CourseWideContext?) {
        _courseWideContext.value = new
    }

    fun updateQuery(new: String) {
        _query.value = new.ifEmpty { null }
    }

    fun updateSortingStrategy(new: MetisSortingStrategy) {
        _sortingStrategy.value = new
    }

    override suspend fun getMetisContext(): MetisContext = metisContext

    private fun insertDateSeparators(pagingList: PagingData<ChatListItem.PostChatListItem>) =
        pagingList.insertSeparators { before: ChatListItem.PostChatListItem?, after: ChatListItem.PostChatListItem? ->
            when {
                before == null && after == null -> null
                before != null && after == null -> {
                    ChatListItem.DateDivider(before.post.creationLocalDate)
                }

                after != null && before != null -> {
                    val beforeDate = before.post.creationLocalDate
                    val afterDate = after.post.creationLocalDate

                    if (beforeDate != afterDate) {
                        ChatListItem.DateDivider(beforeDate)
                    } else null
                }

                else -> null
            }
        }

    private val Post.creationLocalDate: LocalDate get() = creationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
