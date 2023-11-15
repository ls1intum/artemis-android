package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisContentViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

internal class MetisListViewModel(
    initialMetisContext: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val metisContextManager: MetisContextManager,
    metisModificationService: MetisModificationService,
    accountService: AccountService,
    websocketProvider: WebsocketProvider,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    conversationService: ConversationService,
    replyTextStorageService: ReplyTextStorageService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisContentViewModel(
    initialMetisContext,
    websocketProvider,
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    conversationService,
    replyTextStorageService,
    coroutineContext
) {
    private val _filter = MutableStateFlow<List<MetisFilter>>(emptyList())
    val filter: StateFlow<List<MetisFilter>> = _filter

    private val _query = MutableStateFlow<String?>(null)
    val query: StateFlow<String> = _query
        .map(String?::orEmpty)
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

    private val delayedQuery = _query
        .debounce(300.milliseconds)
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val _sortingStrategy = MutableStateFlow(MetisSortingStrategy.DATE_DESCENDING)
    val sortingStrategy: StateFlow<MetisSortingStrategy> = _sortingStrategy

    private val _courseWideContext = MutableStateFlow<CourseWideContext?>(null)
    val courseWideContext: StateFlow<CourseWideContext?> = _courseWideContext

    private val standaloneMetisContext: Flow<StandalonePostsContext> = combine(
        metisContext,
        _filter,
        delayedQuery,
        _sortingStrategy,
        _courseWideContext
    ) { metisContext, filter, query, sortingStrategy, courseWideContext ->
        StandalonePostsContext(
            metisContext = metisContext,
            filter = filter,
            query = query,
            sortingStrategy = sortingStrategy,
            courseWideContext = courseWideContext
        )
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val pagingDataInput: Flow<PagingDataInput> = combine(
        accountService.authenticationData,
        serverConfigurationService.serverUrl,
        serverConfigurationService.host,
        standaloneMetisContext,
        MetisListViewModel::PagingDataInput
    )
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

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
            clientIdOrDefault,
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
                        metisContext = pagingDataInput.standalonePostsContext.metisContext
                    )
                }
            )
                .flow
                .cachedIn(viewModelScope + coroutineContext)
                .map { pagingList -> pagingList.map(ChatListItem::PostChatListItem) }
                .map(::insertDateSeparators)
        }
            .shareIn(viewModelScope + coroutineContext, SharingStarted.Lazily, replay = 1)

    init {
        viewModelScope.launch(coroutineContext) {
            combine(
                serverConfigurationService.host,
                metisContext,
                ::Pair
            ).collectLatest { (host, metisContext) ->
                metisContextManager.updatePosts(host, metisContext, this@MetisListViewModel.coroutineContext)
            }
        }
    }

    fun createPost(): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val postText = newMessageText.first()

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

    fun updateQuery(new: String) {
        _query.value = new.ifEmpty { null }
    }

    override suspend fun getPostId(): Long? = null // by definition we are list, so no post

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

    private val PostPojo.creationLocalDate: LocalDate get() = creationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
