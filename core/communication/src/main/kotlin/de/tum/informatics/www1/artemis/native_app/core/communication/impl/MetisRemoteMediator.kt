package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post

@OptIn(ExperimentalPagingApi::class)
class MetisRemoteMediator(
    private val context: MetisService.StandalonePostsContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val authToken: String,
    private val serverUrl: String,
    private val host: String
) : RemoteMediator<Int, Post>() {

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Post>): MediatorResult {
        val pageSize = state.config.pageSize

        val nextPageIndex = when (loadType) {
            LoadType.REFRESH -> {
                0
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                if (state.isEmpty()) return MediatorResult.Success(endOfPaginationReached = true)
                val loadedItemsCount = metisStorageService.getCachedPostCount(host, context.metisContext)
                (loadedItemsCount - (loadedItemsCount % pageSize)) / pageSize
            }
        }

        val loadedPosts = when (val networkResponse = metisService.getPosts(
            standalonePostsContext = context,
            pageSize = pageSize,
            pageNum = nextPageIndex,
            authToken = authToken,
            serverUrl = serverUrl
        )) {
            is NetworkResponse.Failure -> return MediatorResult.Error(networkResponse.exception)
            is NetworkResponse.Response -> networkResponse.data
        }

        metisStorageService.insertOrUpdatePosts(
            host = host,
            metisContext = context.metisContext,
            posts = loadedPosts,
            clearPreviousPosts = loadType == LoadType.REFRESH
        )

        return MediatorResult.Success(
            endOfPaginationReached = loadedPosts.isEmpty()
        )
    }
}