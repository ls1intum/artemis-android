package de.tum.informatics.www1.artemis.native_app.core.communication.ui.list

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisContextManager
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisRemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

internal class MetisListViewModel(
    val metisContext: MetisContext,
    private val websocketProvider: WebsocketProvider,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val metisContextManager: MetisContextManager,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider
) : MetisViewModel(
    metisService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider
),
    KoinComponent {

    private val _filter = MutableStateFlow<List<MetisFilter>>(emptyList())
    val filter: Flow<List<MetisFilter>> = _filter

    private val _query = MutableStateFlow<String?>(null)
    val query: Flow<String> = _query.map(String?::orEmpty)

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
    val sortingStrategy: Flow<MetisSortingStrategy> = _sortingStrategy

    private val _courseWideContext = MutableStateFlow<CourseWideContext?>(null)
    val courseWideContext: Flow<CourseWideContext?> = _courseWideContext

    private val standaloneMetisContext: Flow<MetisService.StandalonePostsContext> = combine(
        _filter,
        delayedQuery,
        _sortingStrategy,
        _courseWideContext
    ) { filter, query, sortingStrategy, courseWideContext ->
        MetisService.StandalonePostsContext(
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
        val standalonePostsContext: MetisService.StandalonePostsContext
    )

    @OptIn(ExperimentalPagingApi::class)
    val postPagingData: Flow<PagingData<Post>> =
        transformLatest(
            pagingDataInput.withPrevious(),
            accountService.authToken,
            clientId.map { it.orElse(0L) }
        ) { (previousPagingDataInput, pagingDataInput), authToken, clientId ->
            var doneRefresh = false
            metisContextManager.getContextDataActionFlow(metisContext)
                .collect { currentDataAction ->
                    val performInitialRefresh = when (currentDataAction) {
                        // If we already did a refresh we can ignore keep
                        MetisContextManager.CurrentDataAction.Keep -> if (doneRefresh) {
                            return@collect
                        } else false

                        MetisContextManager.CurrentDataAction.Outdated -> return@collect // there is nothing we can do here
                        MetisContextManager.CurrentDataAction.Refresh -> {
                            doneRefresh = true
                            true
                        }
                    } || (previousPagingDataInput != pagingDataInput)

                    val newPager = Pager(
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
                            performInitialRefresh = performInitialRefresh
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

                    emit(newPager)
                }
        }
            .flatMapLatest { it.flow.cachedIn(viewModelScope) }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val isDataOutdated: Flow<Boolean> =
        metisContextManager.getContextDataActionFlow(metisContext).map {
            when (it) {
                MetisContextManager.CurrentDataAction.Keep -> false
                MetisContextManager.CurrentDataAction.Outdated -> true
                MetisContextManager.CurrentDataAction.Refresh -> false
            }
        }

    init {
        viewModelScope.launch {
            serverConfigurationService.host.collectLatest { host ->
                metisContextManager.updatePosts(host, metisContext)
            }
        }
    }

    fun requestRefreshWebsocket() {
        websocketProvider.requestTryReconnect()
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
}