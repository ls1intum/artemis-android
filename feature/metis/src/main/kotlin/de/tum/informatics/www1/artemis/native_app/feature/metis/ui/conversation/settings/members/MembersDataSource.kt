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

    override fun getRefreshKey(state: PagingState<Int, ConversationUser>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            (anchorPosition / state.config.pageSize) + 1
        } ?: 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ConversationUser> {
        return when (
            val membersNetworkResponse = conversationService.getMembers(
                courseId,
                conversationId,
                query,
                params.loadSize,
                params.key ?: 0,
                authToken,
                serverUrl
            )
        ) {
            is NetworkResponse.Response -> LoadResult.Page(membersNetworkResponse.data, null, null)
            is NetworkResponse.Failure -> LoadResult.Error(membersNetworkResponse.exception)
        }
    }
}