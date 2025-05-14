package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import io.ktor.client.statement.HttpResponse

interface MetisService {

    /**
     * Performs a paged call to get the list of standalone posts available.
     */
    suspend fun getPosts(
        standalonePostsContext: StandalonePostsContext,
        pageSize: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<StandalonePost>>

    /**
     * Request a single post from the server.
     */
    suspend fun getPost(
        metisContext: MetisContext,
        serverSidePostId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    suspend fun getForwardedMessagesByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        postType: PostingType,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ForwardedMessage>>

    suspend fun createForwardedMessage(
        metisContext: MetisContext,
        forwardedMessage: ForwardedMessage,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpResponse>

    suspend fun getPostsByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<StandalonePost>>

    suspend fun getAnswerPostsByIds(
        metisContext: MetisContext,
        answerPostIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<AnswerPost>>

    /**
     * Request a link preview for a given url.
     */
    suspend fun fetchLinkPreview(
        url: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<LinkPreview?>

    /**
     * The metis context needed to query standalone posts.
     * @param query if not null the posts will be filtered to contain the given query.
     */
    data class StandalonePostsContext(
        val metisContext: MetisContext,
        val filter: MetisFilter,
        val query: String?,
        val sortingStrategy: MetisSortingStrategy = MetisSortingStrategy.DATE_DESCENDING,
        val courseWideContext: CourseWideContext? = null
    )
}