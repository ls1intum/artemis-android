package de.tum.informatics.www1.artemis.native_app.feature.metistest

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

class MetisServiceStub(
    var posts: List<StandalonePost> = emptyList(),
    private val forwardedMessages: List<ForwardedMessage> = emptyList()
): MetisService {

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

    override suspend fun getForwardedMessagesByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        postType: PostingType,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ForwardedMessage>> {
        return NetworkResponse.Response(forwardedMessages)
    }

    override suspend fun getPostsByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<StandalonePost>> {
        return NetworkResponse.Response(posts)
    }

    override suspend fun getAnswerPostsByIds(
        metisContext: MetisContext,
        answerPostIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<AnswerPost>> {
        return NetworkResponse.Response(emptyList())
    }

    override suspend fun fetchLinkPreview(
        url: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<LinkPreview?> {
        return NetworkResponse.Response(null)
    }
}