package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.paging.PagingSource
import androidx.paging.PagingSourceFactory
import androidx.paging.PagingState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MetisStorageServiceStub(val posts: List<PostPojo>) : MetisStorageService {
    override suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        clearPreviousPosts: Boolean
    ) = Unit

    override suspend fun updatePost(
        host: String,
        metisContext: MetisContext,
        post: StandalonePost
    ) = Unit

    override suspend fun insertLiveCreatedPost(
        host: String,
        metisContext: MetisContext,
        post: StandalonePost
    ): String? = null

    override suspend fun deletePosts(host: String, postIds: List<Long>) = Unit

    override fun getStoredPosts(
        serverId: String,
        clientId: Long,
        filter: List<MetisFilter>,
        sortingStrategy: MetisSortingStrategy,
        query: String?,
        metisContext: MetisContext
    ): PagingSource<Int, PostPojo> = object : PagingSource<Int, PostPojo>() {
        override fun getRefreshKey(state: PagingState<Int, PostPojo>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostPojo> {
            return LoadResult.Page(posts, null, null)
        }
    }

    override fun getStandalonePost(clientPostId: String): Flow<PostPojo?> = flowOf()

    override suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int = posts.size

    override suspend fun getServerSidePostId(host: String, clientSidePostId: String): Long = 0L

    override suspend fun getClientSidePostId(
        host: String,
        serverSidePostId: Long,
        postingType: BasePostingEntity.PostingType
    ): String = ""
}
