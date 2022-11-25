package de.tum.informatics.www1.artemis.native_app.core.communication

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost

class StandalonePostPagingSource: PagingSource<Int, StandalonePost>() {

    override fun getRefreshKey(state: PagingState<Int, StandalonePost>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StandalonePost> {
        TODO("Not yet implemented")
    }
}