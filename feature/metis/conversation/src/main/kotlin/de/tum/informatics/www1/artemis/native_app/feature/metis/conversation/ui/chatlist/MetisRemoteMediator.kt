package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService.StandalonePostsContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import java.lang.RuntimeException

@OptIn(ExperimentalPagingApi::class)
internal class MetisRemoteMediator(
    private val context: MetisContext,
    private val metisService: MetisService,
    private val metisStorageService: MetisStorageService,
    private val authToken: String,
    private val serverUrl: String,
    private val host: String
) : RemoteMediator<Int, PostPojo>() {

    override suspend fun initialize(): InitializeAction {
        return if (metisStorageService.getCachedPostCount(host, context) == 0) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostPojo>
    ): MediatorResult {
        val pageSize = state.config.pageSize

        val nextPageIndex = when (loadType) {
            REFRESH -> {
                return MediatorResult.Success(false)
            }

            PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            APPEND -> {
                Log.d(TAG, "Appending to list")
                val loadedItemsCount =
                    metisStorageService.getCachedPostCount(host, context)
                (loadedItemsCount - (loadedItemsCount % pageSize)) / pageSize
            }
        }

        val loadedPosts =
            when (val networkResponse = loadPosts(
                serverUrl = serverUrl,
                authToken = authToken,
                metisService = metisService,
                context = context,
                pageSize = pageSize, pageNum = nextPageIndex
            )) {
                is NetworkResponse.Failure -> return MediatorResult.Error(networkResponse.exception)
                is NetworkResponse.Response -> networkResponse.data
            }

        metisStorageService.insertOrUpdatePosts(
            host = host,
            metisContext = context,
            posts = loadedPosts,
            clearPreviousPosts = false
        )

        return MediatorResult.Success(
            endOfPaginationReached = loadedPosts.size < pageSize
        )
    }

    companion object {
        private const val TAG = "MetisRemoteMediator"

        suspend fun loadPosts(
            metisService: MetisService,
            context: MetisContext,
            serverUrl: String,
            authToken: String,
            pageSize: Int,
            pageNum: Int
        ): NetworkResponse<List<StandalonePost>> {
            Log.d(
                TAG,
                "Loading posts from server: pageSize=$pageSize, pageNum=$pageNum -> offset=${pageNum * pageSize}"
            )

            return metisService.getPosts(
                standalonePostsContext = StandalonePostsContext(
                    metisContext = context,
                    filter = emptyList(),
                    query = null,
                    sortingStrategy = MetisSortingStrategy.DATE_DESCENDING,
                    courseWideContext = null
                ),
                pageSize = pageSize,
                pageNum = pageNum,
                authToken = authToken,
                serverUrl = serverUrl
            )
        }
    }
}