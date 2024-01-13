package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

class MetisSearchPagingSource(
    private val metisService: MetisService,
    private val context: MetisService.StandalonePostsContext,
    private val authToken: String,
    private val serverUrl: String
) : PagingSource<Int, StandalonePost>() {
    override fun getRefreshKey(state: PagingState<Int, StandalonePost>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StandalonePost> {
        val page = params.key ?: 0

        return metisService.getPosts(
            standalonePostsContext = context,
            pageSize = params.loadSize,
            pageNum = params.key ?: 0,
            authToken = authToken,
            serverUrl = serverUrl
        )
            .map(
                mapSuccess = { loadedPosts ->
                    LoadResult.Page(
                        data = loadedPosts,
                        prevKey = if (page > 0) page - 1 else null,
                        nextKey = if (loadedPosts.size == params.loadSize) page + 1 else null
                    )
                },
                mapFailure = { LoadResult.Error(it) }
            )
    }
}
