package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus

interface SavedPostService {

    suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<BasePost>>

    suspend fun savePost(
        post: IBasePost,
        authToken: String,
        serverUrl: String
    ) = savePost(
        postId = getPostId(post),
        postType = getPostType(post),
        authToken = authToken,
        serverUrl = serverUrl
    )

    suspend fun savePost(
        postId: Long,
        postType: SavedPostPostingType,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit>

    suspend fun deleteSavedPost(
        post: IBasePost,
        authToken: String,
        serverUrl: String
    ) = deleteSavedPost(
        postId = getPostId(post),
        postType = getPostType(post),
        authToken = authToken,
        serverUrl = serverUrl
    )

    suspend fun deleteSavedPost(
        postId: Long,
        postType: SavedPostPostingType,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit>

    suspend fun changeSavedPostStatus(
        post: BasePost,
        status: SavedPostStatus,
        authToken: String,
        serverUrl: String
    ) = changeSavedPostStatus(
        postId = post.id ?: -1,
        postType = when (post) {
            is StandalonePost -> SavedPostPostingType.POST
            is AnswerPost -> SavedPostPostingType.ANSWER
        },
        status = status,
        authToken = authToken,
        serverUrl = serverUrl,
    )

    suspend fun changeSavedPostStatus(
        postId: Long,
        postType: SavedPostPostingType,
        status: SavedPostStatus,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Unit>



    private fun getPostId(post: IBasePost): Long {
        return post.serverPostId ?: throw IllegalArgumentException("Post must have a server id")
    }

    private fun getPostType(post: IBasePost): SavedPostPostingType {
        return when (post) {
            is IStandalonePost -> SavedPostPostingType.POST
            is IAnswerPost -> SavedPostPostingType.ANSWER
            else -> throw IllegalArgumentException("Post type not supported")
        }
    }

}