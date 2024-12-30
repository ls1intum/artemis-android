package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.appendPathSegments

class SavedPostServiceImpl(
    private val ktorProvider: KtorProvider
) : de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.SavedPostService {

    override suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<BasePost>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "saved-posts",
                        courseId.toString(),
                        status.ordinal.toString()
                    )
                }

                cookieAuth(authToken)
            }.body().
        }
    }

    override suspend fun savePost(
        postId: Long,
        postType: SavedPostPostingType,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "saved-posts",
                        postId.toString(),
                        postType.ordinal.toString(),
                    )
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun deleteSavedPost(
        postId: Long,
        postType: SavedPostPostingType,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            ktorProvider.ktorClient.delete(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "saved-posts",
                        postId.toString(),
                        postType.ordinal.toString(),
                    )
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun changeSavedPostStatus(
        postId: Long,
        postType: SavedPostPostingType,
        status: SavedPostStatus,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "saved-posts",
                        postId.toString(),
                        postType.ordinal.toString()
                    )

                    parameter("status", status.ordinal)
                }

                cookieAuth(authToken)
            }.body()
        }
    }

}