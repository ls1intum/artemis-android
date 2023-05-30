package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
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
import kotlin.time.Duration.Companion.milliseconds

internal class MetisListViewModel(
    val metisContext: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val metisContextManager: MetisContextManager,
    private val metisModificationService: MetisModificationService,
    private val accountService: AccountService,
    websocketProvider: WebsocketProvider,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService
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
    val postPagingData: Flow<PagingData<Post>> =
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
            ).flow.cachedIn(viewModelScope)
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
            if (metisContext !is MetisContext.Conversation) return@async MetisModificationFailure.CREATE_POST

            val conversation =
                loadConversation(metisContext) ?: return@async MetisModificationFailure.CREATE_POST

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

    fun editPost(post: Post, newText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async {
            if (metisContext !is MetisContext.Conversation) return@async MetisModificationFailure.UPDATE_POST

            val conversation =
                loadConversation(metisContext) ?: return@async MetisModificationFailure.UPDATE_POST

            val newPost = StandalonePost(
                post = post.copy(content = newText),
                conversation = conversation
            )

            metisModificationService.updateStandalonePost(
                context = metisContext,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
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

    private suspend fun loadConversation(metisContext: MetisContext.Conversation) =
        conversationService.getConversation(
            metisContext.courseId,
            metisContext.conversationId,
            accountService.authToken.first(),
            serverConfigurationService.serverUrl.first()
        ).orNull()
}