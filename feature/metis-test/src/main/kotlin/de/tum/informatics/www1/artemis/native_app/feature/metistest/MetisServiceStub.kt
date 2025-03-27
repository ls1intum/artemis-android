package de.tum.informatics.www1.artemis.native_app.feature.metistest

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

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

    override suspend fun createForwardedMessage(
        metisContext: MetisContext,
        forwardedMessage: ForwardedMessage,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpResponse> {
        return NetworkResponse.Response(HTTPResponseStub())
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

private class HTTPResponseStub @InternalAPI constructor(
    override val call: HttpClientCall,
    override val coroutineContext: CoroutineContext,
    override val headers: Headers,
    @InternalAPI override val rawContent: ByteReadChannel,
    override val requestTime: GMTDate,
    override val responseTime: GMTDate,
    override val status: HttpStatusCode,
    override val version: HttpProtocolVersion
): HttpResponse() {

    @OptIn(InternalAPI::class)
    constructor(): this(
        call = HttpClientCall(HttpClient()),
        coroutineContext = Job(),
        headers = Headers.Empty,
        rawContent = ByteReadChannel.Empty,
        requestTime = GMTDate(0),
        responseTime = GMTDate(0),
        status = HttpStatusCode.OK,
        version = HttpProtocolVersion.HTTP_1_1
    )
}