package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.appendPathSegments

class SavedPostServiceImpl(
    private val ktorProvider: KtorProvider
) : SavedPostService {

    override suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<SavedPost>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.SavedPosts.path
                    )
                }
                parameter("status", status.toString())
                parameter("courseId", courseId.toString())

                cookieAuth(authToken)
            }.body()
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
                        *Api.Communication.SavedPosts.path,
                        postId.toString()
                    )
                }
                parameter("type", postType.toString())

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
                        *Api.Communication.SavedPosts.path,
                        postId.toString()
                    )
                }
                parameter("type", postType.toString())

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
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.SavedPosts.path,
                        postId.toString()
                    )
                    parameter("type", postType.toString())
                    parameter("status", status.toString())
                }

                cookieAuth(authToken)
            }.body()
        }
    }

}