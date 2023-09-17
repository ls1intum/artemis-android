package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.Post

@OptIn(ExperimentalPagingApi::class)
class MetisRemoteMediator(
    private val context: StandalonePostsContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val authToken: String,
    private val serverUrl: String,
    private val host: String,
    private val performInitialRefresh: Boolean
) : RemoteMediator<Int, Post>() {

    override suspend fun initialize(): InitializeAction {
        return if (performInitialRefresh) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Post>): MediatorResult {
        val pageSize = state.config.pageSize

        val nextPageIndex = when (loadType) {
            LoadType.REFRESH -> {
                0
            }

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                if (state.isEmpty()) return MediatorResult.Success(endOfPaginationReached = true)
                val loadedItemsCount =
                    metisStorageService.getCachedPostCount(host, context.metisContext)
                (loadedItemsCount - (loadedItemsCount % pageSize)) / pageSize
            }
        }

        val loadedPosts = when (val networkResponse =
            metisService.getPosts(
                standalonePostsContext = context,
                pageSize = pageSize,
                pageNum = nextPageIndex,
                authToken = authToken,
                serverUrl = serverUrl
            )
        ) {
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
            endOfPaginationReached = loadedPosts.size < pageSize
        )
    }
}