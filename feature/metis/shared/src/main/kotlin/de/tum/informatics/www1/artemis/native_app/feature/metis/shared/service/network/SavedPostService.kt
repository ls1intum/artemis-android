package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
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
        post: BasePost,
        authToken: String,
        serverUrl: String
    ) = savePost(
        postId = post.id ?: -1,
        postType = when (post) {
            is StandalonePost -> SavedPostPostingType.POST
            is AnswerPost -> SavedPostPostingType.ANSWER
        },
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
        post: BasePost,
        authToken: String,
        serverUrl: String
    ) = deleteSavedPost(
        postId = post.id ?: -1,
        postType = when (post) {
            is StandalonePost -> SavedPostPostingType.POST
            is AnswerPost -> SavedPostPostingType.ANSWER
        },
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

}