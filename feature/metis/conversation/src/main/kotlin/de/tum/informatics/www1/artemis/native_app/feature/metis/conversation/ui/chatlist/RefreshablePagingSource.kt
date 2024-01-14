package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo

class RefreshablePagingSource(
    private val delegate: PagingSource<Int, PostPojo>,
    private val onPageLoaded: (offset: Int, size: Int) -> Unit
) : PagingSource<Int, PostPojo>() {

    companion object {
        private const val TAG = "RefreshablePagingSource"
    }

    override val jumpingSupported: Boolean
        get() = delegate.jumpingSupported
    override val keyReuseSupported: Boolean
        get() = delegate.keyReuseSupported

    init {
        delegate.registerInvalidatedCallback { invalidate() }
        registerInvalidatedCallback { delegate.invalidate() }
    }

    override fun getRefreshKey(state: PagingState<Int, PostPojo>): Int? =
        delegate.getRefreshKey(state)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostPojo> {
        Log.d(TAG, "Loading page: offset=${params.key}, size=${params.loadSize}")
        onPageLoaded(params.key ?: 0, params.loadSize)

        return delegate.load(params)
    }
}