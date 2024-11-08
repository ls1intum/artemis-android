package de.tum.informatics.www1.artemis.native_app.feature.metistest

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MetisServiceStub : MetisService {

    private var posts: List<StandalonePost> = emptyList()

    fun setPosts(posts: List<StandalonePost>) {
        this.posts = posts
    }

    override suspend fun getPosts(
        standalonePostsContext: MetisService.StandalonePostsContext,
        pageSize: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<StandalonePost>> {
        return NetworkResponse.Response(posts)
    }

    override suspend fun getPost(
        metisContext: MetisContext,
        serverSidePostId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return NetworkResponse.Response(posts.first())
    }

    override fun subscribeToPostUpdates(metisContext: MetisContext): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>> {
        return flowOf()
    }
}