package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService

internal class MembersDataSource(
    private val courseId: Long,
    private val conversationId: Long,
    private val serverUrl: String,
    private val authToken: String,
    private val query: String,
    private val conversationService: ConversationService
) : PagingSource<Int, ConversationUser>() {

    companion object {
        private const val MAX_PAGE_SIZE = 20
    }

    override fun getRefreshKey(state: PagingState<Int, ConversationUser>): Int {
        return state.anchorPosition?.let { anchorPosition ->
            (anchorPosition / state.config.pageSize) + 1
        } ?: 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ConversationUser> {
        val pageNum = params.key ?: 0

        val pageSize = params.loadSize.coerceAtMost(MAX_PAGE_SIZE)
        return when (
            val membersNetworkResponse = conversationService.getMembers(
                courseId,
                conversationId,
                query,
                pageSize,
                pageNum,
                authToken,
                serverUrl
            )
        ) {
            is NetworkResponse.Response -> LoadResult.Page(
                membersNetworkResponse.data,
                null,
                if (membersNetworkResponse.data.size >= pageSize) pageNum + 1 else null
            )

            is NetworkResponse.Failure -> LoadResult.Error(membersNetworkResponse.exception)
        }
    }
}