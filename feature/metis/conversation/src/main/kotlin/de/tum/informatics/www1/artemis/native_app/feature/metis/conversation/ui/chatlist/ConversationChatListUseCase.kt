package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

class ConversationChatListUseCase(
    private val viewModelScope: CoroutineScope,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    metisContext: MetisContext,
    onRequestReload: MutableSharedFlow<Unit>,
    clientIdOrDefault: Flow<Long>,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
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

    private val standaloneMetisContext: Flow<StandalonePostsContext> = combine(
        _filter,
        delayedQuery
    ) { filter, query ->
        StandalonePostsContext(
            metisContext = metisContext,
            filter = filter,
            query = query
        )
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val pagingDataInput: Flow<PagingDataInput> = combine(
        accountService.authenticationData,
        serverConfigurationService.serverUrl,
        serverConfigurationService.host,
        standaloneMetisContext,
        ConversationChatListUseCase::PagingDataInput
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
            val config = PagingConfig(
                pageSize = 20
            )

            if (pagingDataInput.standalonePostsContext.query.isNullOrBlank()) {
                Pager(
                    config = config,
                    remoteMediator = MetisRemoteMediator(
                        context = pagingDataInput.standalonePostsContext.metisContext,
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
                            metisContext = pagingDataInput.standalonePostsContext.metisContext
                        )
                    }
                )
                    .flow
                    .cachedIn(viewModelScope + coroutineContext)
            } else {
                Pager(
                    config = config,
                    initialKey = 0,
                    remoteMediator = null,
                    pagingSourceFactory = {
                        MetisSearchPagingSource(
                            metisService = metisService,
                            context = pagingDataInput.standalonePostsContext,
                            authToken = authToken,
                            serverUrl = pagingDataInput.serverUrl
                        )
                    }
                )
                    .flow
                    .cachedIn(viewModelScope + coroutineContext)
            }
                .map { pagingList -> pagingList.map(ChatListItem::PostChatListItem) }
                .map(::insertDateSeparators)
        }
            .shareIn(viewModelScope + coroutineContext, SharingStarted.Lazily, replay = 1)

    fun updateQuery(new: String) {
        _query.value = new.ifEmpty { null }
    }

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

    private val IStandalonePost.creationLocalDate: LocalDate
        get() = (creationDate ?: Instant.fromEpochMilliseconds(0))
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
}
