package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.util.ForwardedMessagesHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo

class RefreshablePagingSource(
    private val delegate: PagingSource<Int, PostPojo>,
    private val onPageLoaded: (offset: Int, size: Int) -> Unit,
    private val forwardedMessagesHandler: ForwardedMessagesHandler
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

        return when (val result = delegate.load(params)) {
            is LoadResult.Page -> {

                forwardedMessagesHandler.extractForwardedMessages(result.data)
                forwardedMessagesHandler.loadForwardedMessages(PostingType.POST)

                LoadResult.Page(
                    data = result.data,
                    prevKey = result.prevKey,
                    nextKey = result.nextKey
                )
            }
            else -> result
        }
    }
}