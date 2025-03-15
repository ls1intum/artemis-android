package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus

interface SavedPostService {

    suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<SavedPost>>

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
        post: ISavedPost,
        status: SavedPostStatus,
        authToken: String,
        serverUrl: String
    ) = changeSavedPostStatus(
        postId = getPostId(post),
        postType = getPostType(post),
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
            is SavedPost -> post.postingType
            else -> throw IllegalArgumentException("Post type not supported")}
    }

}