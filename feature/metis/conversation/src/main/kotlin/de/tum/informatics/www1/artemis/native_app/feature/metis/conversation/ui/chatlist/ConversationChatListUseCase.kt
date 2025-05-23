package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.util.ForwardedMessagesHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.DataStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class ConversationChatListUseCase(
    private val viewModelScope: CoroutineScope,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val metisContext: MetisContext.Conversation,
    onRequestSoftReload: Flow<Unit>,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    conversation: StateFlow<DataState<Conversation>>,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val courseId: Long
) {
    companion object {
        private const val TAG = "ConversationChatListUseCase"

        private const val PAGE_SIZE = 20
    }

    private val _filter = MutableStateFlow<MetisFilter>(MetisFilter.All)
    val filter: StateFlow<MetisFilter> = _filter

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

    private val highestVisiblePostIndex = MutableStateFlow(0)

    private val unreadMessagesCountFlow: Flow<Long> = conversation.map {
        it.bind { conversation ->
            conversation.unreadMessagesCount ?: 0L
        }.orElse(0L)
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, 0L)

    // We store this in addition to the unreadPostCount, because as new posts come in, the unreadPostCount will become outdated.
    private var lastAlreadyReadPostId: Long? = null

    fun resetLastAlreadyReadPostId() {
        lastAlreadyReadPostId = null
    }

    @OptIn(ExperimentalPagingApi::class)
    val postPagingData: Flow<PagingData<ChatListItem>> =
        pagingDataInput.flatMapLatest { pagingDataInput ->
            val config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = true
            )
            val forwardedMessagesHandler = ForwardedMessagesHandler(
                metisService = metisService,
                metisContext = metisContext,
                authToken = pagingDataInput.authenticationData.authToken,
                serverUrl = pagingDataInput.serverUrl
            )

            val isSearchActive = !pagingDataInput.standalonePostsContext.query.isNullOrBlank()
            val isFilteringActive = pagingDataInput.standalonePostsContext.filter !is MetisFilter.All
            val pagerFlow = if (!isSearchActive && !isFilteringActive) {
                Pager(
                    config = config,
                    remoteMediator = MetisRemoteMediator(
                        context = metisContext,
                        metisService = metisService,
                        metisStorageService = metisStorageService,
                        authToken = pagingDataInput.authenticationData.authToken,
                        serverUrl = pagingDataInput.serverUrl,
                        host = pagingDataInput.host
                    ),
                    pagingSourceFactory = {
                        // We use a wrapper paging source that reports which page has been loaded and shown to the user.
                        // This way, we know which page to refresh next.
                        RefreshablePagingSource(
                            delegate = metisStorageService.getStoredPosts(
                                serverId = pagingDataInput.host,
                                metisContext = metisContext
                            ),
                            onPageLoaded = { offset, size ->
                                highestVisiblePostIndex.update { currentHighestIndex ->
                                    max(currentHighestIndex, offset + size)
                                }
                            },
                            forwardedMessagesHandler = forwardedMessagesHandler
                        )
                    }
                )
                    .flow
                    .mapIndexedPosts()
                    .resolveForwardedPosts(forwardedMessagesHandler)
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
                            authToken = pagingDataInput.authenticationData.authToken,
                            serverUrl = pagingDataInput.serverUrl,
                            forwardedMessagesHandler = forwardedMessagesHandler
                        )
                    }
                )
                    .flow
                    .mapIndexedPosts()
                    .resolveForwardedPosts(forwardedMessagesHandler)
                    .cachedIn(viewModelScope + coroutineContext)
            }

            pagerFlow
                .map(::insertDateSeparators)
                .map {
                    if (!isSearchActive) {
                        insertUnreadSeparator(it)
                    } else {
                        it
                    }
                }
        }
            .shareIn(viewModelScope + coroutineContext, SharingStarted.Lazily, replay = 1)


    private fun Flow<PagingData<out IStandalonePost>>.mapIndexedPosts(): Flow<PagingData<ChatListItem.PostItem.IndexedItem>> {
        // TODO: this indexing seems to work, BUT if a chat has between 40 and 60 posts, the pager messes something up
        //  https://github.com/ls1intum/artemis-android/issues/392
        return this.map { pagingData ->
            var indexCounter = 0L
            pagingData.map { post ->
                if (post.hasForwardedMessages == true) {
                    ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage(
                        post,
                        post.answers.orEmpty(),
                        indexCounter++,
                        emptyList(), // This is only a placeholder, forwarded messages will be resolved in the next step
                        courseId
                    )
                } else {
                    ChatListItem.PostItem.IndexedItem.Post(post, post.answers.orEmpty(), indexCounter++)
                }
            }
        }
    }

    private fun Flow<PagingData<ChatListItem.PostItem.IndexedItem>>.resolveForwardedPosts(forwardedMessagesHandler: ForwardedMessagesHandler): Flow<PagingData<ChatListItem.PostItem.IndexedItem>> {
        return this.map { pagingData ->
            pagingData.map { chatListItem ->
                forwardedMessagesHandler.resolveForwardedMessagesForIndexedPost(chatListItem)
            }
        }
    }

    /**
     * This flow handles reloading the visible posts from the server.
     * It is invoked when a soft reload is requested.
     *
     * It loads the necessary data from the server and updates the db.
     *
     * Instead of directly emitting a DataState, it instead emits a wrapper that also contains which
     * highestVisiblePostIndex was used. This is then used in [softReloadOnScrollDataStatus].
     */
    private val softReloadOnRequestDataStatus: Flow<SoftReloadOnRequestResult> = onRequestSoftReload
        .transformLatest {
            emit(SoftReloadOnRequestResult(DataStatus.Loading, 0))
            val successfullyLoadedNewPosts =
                loadNewlyCreatedPosts(pageSize = PAGE_SIZE) is NetworkResponse.Response

            val highestVisiblePostIndex = highestVisiblePostIndex.value
            val successfullyLoadedExistingPosts = refreshExistingVisiblePosts(
                startVisibleIndex = 0, // start at the very bottom, as the user has seen all posts until the [highestVisiblePostIndex]
                highestVisibleIndex = highestVisiblePostIndex,
                pageSize = PAGE_SIZE
            )

            val resultStatus =
                if (successfullyLoadedNewPosts && successfullyLoadedExistingPosts) DataStatus.UpToDate else DataStatus.Outdated

            emit(SoftReloadOnRequestResult(resultStatus, highestVisiblePostIndex))
        }
        .onStart { emit(SoftReloadOnRequestResult(DataStatus.Outdated, 0)) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * This flow handles reloading the visible posts from the server.
     * It is invoked when the user scrolls through their chat list.
     *
     * This flow is the logical extension to [softReloadOnRequestDataStatus]: When the user scrolls through their long chat history,
     * not all posts have been refreshed by [softReloadOnRequestDataStatus]. Therefore, this flow allows to lazily refresh posts only when the user views them.
     *
     * It loads the necessary data from the server and updates the db.
     */
    private val softReloadOnScrollDataStatus: Flow<DataStatus> = softReloadOnRequestDataStatus
        .transformLatest { softReloadOnRequestDataStatus ->
            // Cache to hold for which index we last performed a refresh.
            // Initialized to the value of the onRequest reload.
            // Updated on each successful scroll update.
            var prevHighestVisiblePostIndex = softReloadOnRequestDataStatus.usedHighestVisibleIndex

            // First check if the onRequest soft reload was successful. If not, we do not need to perform the on scroll check.
            // The reason for this is that it the onRequest soft reload will be tried again anyway with the higher post index.
            if (softReloadOnRequestDataStatus.dataStatus == DataStatus.UpToDate) {
                // Repeat this for as long as possible
                highestVisiblePostIndex.collect { highestVisiblePostIndex ->
                    // Only perform an action if there is really a higher post index.
                    if (prevHighestVisiblePostIndex < highestVisiblePostIndex) {
                        emit(DataStatus.Loading)

                        val successfullyLoadedExistingPosts = refreshExistingVisiblePosts(
                            startVisibleIndex = prevHighestVisiblePostIndex,
                            highestVisibleIndex = highestVisiblePostIndex,
                            pageSize = PAGE_SIZE
                        )

                        prevHighestVisiblePostIndex = highestVisiblePostIndex

                        emit(if (successfullyLoadedExistingPosts) DataStatus.UpToDate else DataStatus.Outdated)
                    } else {
                        // Nothing new -> up to date
                        emit(DataStatus.UpToDate)
                    }
                }
            } else {
                // Just forward to emit a value
                emit(softReloadOnRequestDataStatus.dataStatus)
            }

        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * The combined data status of the chat list data. Tells the UI if we are in sync with the server or not.
     */
    val dataStatus: Flow<DataStatus> = combine(
        softReloadOnRequestDataStatus.map { it.dataStatus },
        softReloadOnScrollDataStatus,
        ::minOf
    )

    val bottomPost: StateFlow<PostPojo?> = serverConfigurationService
        .host
        .flatMapLatest { host ->
            metisStorageService.getLatestKnownPost(host, metisContext, true)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateQuery(new: String) {
        _query.value = new.ifEmpty { null }
    }

    fun updateFilter(new: MetisFilter) {
        _filter.value = new
    }

    private fun insertDateSeparators(pagingList: PagingData<ChatListItem.PostItem.IndexedItem>) =
        pagingList.insertSeparators { before: ChatListItem.PostItem.IndexedItem?, after: ChatListItem.PostItem.IndexedItem? ->
            when {
                before == null && after == null -> null
                before != null && after == null -> {
                    ChatListItem.DateDivider((before.post as IStandalonePost).creationLocalDate)
                }

                after != null && before != null -> {
                    val beforeDate = (before.post as IStandalonePost).creationLocalDate
                    val afterDate = (after.post as IStandalonePost).creationLocalDate

                    if (beforeDate != afterDate) {
                        ChatListItem.DateDivider(beforeDate)
                    } else null
                }

                else -> null
            }
        }

    private fun insertUnreadSeparator(pagingList: PagingData<ChatListItem>) =
        pagingList.insertSeparators { _, after: ChatListItem? ->
            // If we already know the id, great
            if (lastAlreadyReadPostId != null) {
                if (after != null && after is ChatListItem.PostItem.IndexedItem && after.post.serverPostId == lastAlreadyReadPostId) {
                    return@insertSeparators ChatListItem.UnreadIndicator
                } else {
                    return@insertSeparators null
                }
            }

            // Otherwise we first need to figure out the id of the last already read post

            val unreadMessagesCount = unreadMessagesCountFlow.first()

            if (unreadMessagesCount == 0L) {
                return@insertSeparators null
            }

            if (after != null && after is ChatListItem.PostItem.IndexedItem && after.index == unreadMessagesCount) {
                lastAlreadyReadPostId = after.post.serverPostId
                return@insertSeparators ChatListItem.UnreadIndicator
            }

            return@insertSeparators null
        }

    /**
     * Tries to load all posts that have been added in the meantime.
     */
    private suspend fun loadNewlyCreatedPosts(pageSize: Int): NetworkResponse<Unit> {
        val serverUrl = serverConfigurationService.serverUrl.first()
        val host = serverConfigurationService.host.first()
        val authToken = accountService.authToken.first()

        Log.d(TAG, "Loading newly created posts")
        val latestKnownPost =
            metisStorageService.getLatestKnownPost(host, metisContext, false).first()

        return if (latestKnownPost == null) {
            Log.d(TAG, "There is no latest known post -> loading all posts.")

            // We have no posts, just load the first page.
            loadAndStorePosts(
                host = host,
                serverUrl = serverUrl,
                authToken = authToken,
                pageSize = pageSize,
                pageNum = 0,
                clearPreviousPosts = false
            ).bind { }
        } else {
            // Load posts until we find the first post that matches,
            val maxPageNum = 10
            var currentPageNum = 0

            val loadedPosts = mutableListOf<StandalonePost>()

            while (true) {
                if (currentPageNum > maxPageNum) {
                    Log.d(
                        TAG,
                        "Reached maximum look back. Performing hard refresh."
                    )

                    // This did not work. Perform hard reset.
                    return loadAndStorePosts(
                        host = host,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        pageNum = 0, pageSize = pageSize,
                        clearPreviousPosts = true
                    ).bind { }
                }

                when (val postResult = MetisRemoteMediator.loadPosts(
                    serverUrl = serverUrl,
                    authToken = authToken,
                    metisService = metisService,
                    pageSize = pageSize,
                    pageNum = currentPageNum,
                    context = metisContext
                )) {
                    is NetworkResponse.Response -> {
                        loadedPosts += postResult.data

                        val reachedEndOfPagination = postResult.data.size < pageSize

                        // Check if the date of any of the loaded posts is smaller than a date we already know.
                        // if this is the case, we are done loading.
                        if (postResult.data.map { it.creationDate }
                                .any { it <= latestKnownPost.creationDate } || reachedEndOfPagination) {
                            Log.d(
                                TAG,
                                "Found known anchor. Storing newly created posts. reachedEndOfPagination=$reachedEndOfPagination"
                            )

                            // We have loaded all missing posts. Insert into db.
                            metisStorageService.insertOrUpdatePostsAndRemoveDeletedPosts(
                                host = host,
                                metisContext = metisContext,
                                posts = loadedPosts,
                                removeAllOlderPosts = true
                            )

                            return NetworkResponse.Response(Unit)
                        }
                    }

                    is NetworkResponse.Failure -> {
                        return NetworkResponse.Failure(postResult.exception)
                    }
                }

                currentPageNum++
            }

            @Suppress("UNREACHABLE_CODE")
            return NetworkResponse.Failure(RuntimeException(""))
        }
    }

    /**
     * @param startVisibleIndex from which post index should we start. Set to higher number than 1 * [pageSize] to skip pages.
     * @param highestVisibleIndex on which index should we end.
     * @returns true if the data was loaded and updated successfully
     */
    private suspend fun refreshExistingVisiblePosts(
        startVisibleIndex: Int,
        highestVisibleIndex: Int,
        pageSize: Int
    ): Boolean {
        val host = serverConfigurationService.host.first()

        val startPageNum = (startVisibleIndex - (startVisibleIndex % pageSize)) / pageSize
        val endPageNum = (highestVisibleIndex - (highestVisibleIndex % pageSize)) / pageSize

        val serverUrl = serverConfigurationService.serverUrl.first()
        val authToken = accountService.authToken.first()

        // Start a coroutine for each page we have to request
        return coroutineScope {
            val updatedPosts = (startPageNum until endPageNum)
                .map { pageNum ->
                    async {
                        MetisRemoteMediator.loadPosts(
                            metisService = metisService,
                            context = metisContext,
                            serverUrl = serverUrl,
                            authToken = authToken,
                            pageSize = pageSize,
                            pageNum = pageNum
                        )
                    }
                }
                .awaitAll()

            if (updatedPosts.all { it is NetworkResponse.Response }) {
                // Successfully loaded all updates
                val newPosts = updatedPosts
                    .filterIsInstance<NetworkResponse.Response<List<StandalonePost>>>()
                    .flatMap { it.data }

                // Edge case, a post at the very end of the list has been removed.
                // If we loaded all posts that exist in this conversation with this update, we must delete posts that are older than the oldest post in this list.
                // This handles the case, if, for example, the oldest 5 posts have been deleted.
                val reachedEndOfPagination = updatedPosts
                    .filterIsInstance<NetworkResponse.Response<List<StandalonePost>>>()
                    .any { it.data.size < pageSize }

                metisStorageService.insertOrUpdatePostsAndRemoveDeletedPosts(
                    host = host,
                    metisContext = metisContext,
                    posts = newPosts,
                    removeAllOlderPosts = reachedEndOfPagination
                )

                true
            } else {
                // At least one page was not loaded successfully
                false
            }
        }
    }

    private suspend fun loadAndStorePosts(
        host: String,
        serverUrl: String,
        authToken: String,
        pageSize: Int,
        pageNum: Int,
        clearPreviousPosts: Boolean
    ): NetworkResponse<Int> {
        val loadedPosts = when (val networkResponse =
            MetisRemoteMediator.loadPosts(
                metisService = metisService,
                context = metisContext,
                serverUrl = serverUrl,
                authToken = authToken,
                pageSize = pageSize,
                pageNum = pageNum
            )
        ) {
            is NetworkResponse.Failure -> return networkResponse.bind { 0 }
            is NetworkResponse.Response -> networkResponse.data
        }

        if (clearPreviousPosts) {
            metisStorageService.insertOrUpdatePostsAndRemoveDeletedPosts(
                host = host,
                metisContext = metisContext,
                posts = loadedPosts,
                removeAllOlderPosts = true
            )
        } else {
            metisStorageService.insertOrUpdatePosts(
                host = host,
                metisContext = metisContext,
                posts = loadedPosts
            )
        }

        return NetworkResponse.Response(loadedPosts.size)
    }

    private val IStandalonePost.creationLocalDate: LocalDate
        get() = (creationDate ?: Instant.fromEpochMilliseconds(0))
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

    private data class SoftReloadOnRequestResult(
        val dataStatus: DataStatus,
        val usedHighestVisibleIndex: Int
    )
}
