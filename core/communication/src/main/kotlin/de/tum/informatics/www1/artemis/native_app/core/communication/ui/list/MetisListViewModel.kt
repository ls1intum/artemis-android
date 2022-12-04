package de.tum.informatics.www1.artemis.native_app.core.communication.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.tum.informatics.www1.artemis.native_app.core.common.combine7
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisRemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.updatePosts
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class MetisListViewModel(
    private val metisContext: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {

    private val _filter = MutableStateFlow<List<MetisFilter>>(emptyList())
    val filter: Flow<List<MetisFilter>> = _filter

    private val _query = MutableStateFlow<String?>(null)
    val query: Flow<String> = _query.map(String?::orEmpty)

    private val _sortingStrategy = MutableStateFlow(MetisSortingStrategy.DATE_DESCENDING)
    val sortingStrategy: Flow<MetisSortingStrategy> = _sortingStrategy

    private val _courseWideContext = MutableStateFlow<CourseWideContext?>(null)
    val courseWideContext: Flow<CourseWideContext?> = _courseWideContext

    private val standalonePostContext: Flow<MetisService.StandalonePostsContext> = combine(
        _filter,
        _query,
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

    @OptIn(ExperimentalPagingApi::class)
    val postPagingData: Flow<PagingData<Post>> = combine7(
        accountService.authenticationData,
        serverConfigurationService.serverUrl,
        serverConfigurationService.host,
        _filter,
        _query,
        _sortingStrategy,
        _courseWideContext
    ) { authData, serverUrl, host, filter, query, sortingStrategy, courseWideContext ->
        val authToken = when (authData) {
            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
            AccountService.AuthenticationData.NotLoggedIn -> return@combine7 null
        }

        Pager(
            config = PagingConfig(
                pageSize = 20
            ),
            remoteMediator = MetisRemoteMediator(
                context = MetisService.StandalonePostsContext(
                    metisContext = metisContext,
                    filter = filter,
                    query = query,
                    sortingStrategy = sortingStrategy,
                    courseWideContext = courseWideContext
                ),
                metisService = metisService,
                metisStorageService = metisStorageService,
                authToken = authToken,
                serverUrl = serverUrl,
                host = host
            ),
            pagingSourceFactory = {
                metisStorageService.getStoredPosts(
                    serverId = host,
                    filter = filter,
                    sortingStrategy = sortingStrategy,
                    query = query,
                    metisContext = metisContext
                )
            }
        )
    }
        .filterNotNull()
        .flatMapLatest { it.flow.cachedIn(viewModelScope) }

    init {
        viewModelScope.launch {
            combine(
                serverConfigurationService.host,
                standalonePostContext
            ) { a, b -> Pair(a, b) }
                .collectLatest { (host, postContext) ->
                    updatePosts(
                        host,
                        metisService,
                        metisStorageService,
                        postContext.metisContext
                    )
                }
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
}