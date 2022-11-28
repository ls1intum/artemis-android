package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.tum.informatics.www1.artemis.native_app.core.common.combine6
import de.tum.informatics.www1.artemis.native_app.core.common.combine7
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.MetisRemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import kotlinx.coroutines.flow.*

internal class MetisViewModel(
    private val metisContext: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {

    private val _filter = MutableStateFlow<List<MetisFilter>>(emptyList())
    private val _query = MutableStateFlow<String?>(null)
    private val _sortingStrategy = MutableStateFlow(MetisSortingStrategy.DATE_DESCENDING)
    private val _courseWideContext = MutableStateFlow<CourseWideContext?>(null)

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
}