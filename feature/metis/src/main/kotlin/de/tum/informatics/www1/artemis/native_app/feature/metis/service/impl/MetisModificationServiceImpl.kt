package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess

internal class MetisModificationServiceImpl(
    private val ktorProvider: KtorProvider
) : MetisModificationService {

    override suspend fun createPost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        context.standalonePostResourceEndpoint
                    )
                }

                contentType(ContentType.Application.Json)

                setBody(post)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun createAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        context.answerPostResourceEndpoint
                    )
                }

                contentType(ContentType.Application.Json)

                setBody(post)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun updateStandalonePost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        context.standalonePostResourceEndpoint
                    )
                    appendPathSegments(post.id.toString())
                }

                contentType(ContentType.Application.Json)
                setBody(post)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun updateAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        context.answerPostResourceEndpoint
                    )
                    appendPathSegments(post.serverPostId.toString())
                }

                contentType(ContentType.Application.Json)
                setBody(post)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun createReaction(
        context: MetisContext,
        post: MetisModificationService.AffectedPost,
        emojiId: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Reaction> {
        val reaction = when (post) {
            is MetisModificationService.AffectedPost.Answer -> {
                Reaction(
                    emojiId = emojiId,
                    answerPost = AnswerPost(id = post.postId)
                )
            }

            is MetisModificationService.AffectedPost.Standalone -> {
                Reaction(
                    emojiId = emojiId,
                    standalonePost = StandalonePost(id = post.postId)
                )
            }
        }

        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        "postings",
                        "reactions"
                    )
                }

                setBody(reaction)
                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.delete(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        "postings",
                        "reactions",
                        reactionId.toString()
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }
                .status
                .isSuccess()
        }
    }

    override suspend fun deletePost(
        context: MetisContext,
        post: MetisModificationService.AffectedPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> {
        val identifier = when (post) {
            is MetisModificationService.AffectedPost.Answer -> "answer-messages"
            is MetisModificationService.AffectedPost.Standalone -> "messages"
        }

        return performNetworkCall {
            ktorProvider.ktorClient.delete(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        identifier,
                        post.postId.toString()
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }
                .status
                .isSuccess()
        }
    }
}