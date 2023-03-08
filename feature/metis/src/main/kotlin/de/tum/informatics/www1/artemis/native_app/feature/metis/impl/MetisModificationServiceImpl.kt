package de.tum.informatics.www1.artemis.native_app.feature.metis.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.Reaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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

    override suspend fun updatePost(
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
                    appendPathSegments(post.id.toString())
                }

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
            }.body()
        }
    }

    override suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
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

            Unit
        }
    }
}