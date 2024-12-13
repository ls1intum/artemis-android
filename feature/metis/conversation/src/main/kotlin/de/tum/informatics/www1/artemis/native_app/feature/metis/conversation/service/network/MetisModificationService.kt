package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.FileUploadResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost

/**
 * Service that can modify metis posts.
 */
interface MetisModificationService {
    suspend fun createPost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    suspend fun updateStandalonePost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    suspend fun updatePostDisplayPriority(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    suspend fun createAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost>

    suspend fun updateAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost>

    /**
     * Returns null if no error occurred.
     */
    suspend fun createReaction(
        context: MetisContext,
        post: AffectedPost,
        emojiId: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Reaction>

    /**
     * Returns null if no error occurred.
     */
    suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean>

    suspend fun deletePost(
        context: MetisContext,
        post: AffectedPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean>

    suspend fun uploadFileOrImage(
        context: MetisContext.Conversation,
        fileBytes: ByteArray,
        fileName: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<FileUploadResponse>

    sealed class AffectedPost {
        abstract val postId: Long

        data class Standalone(override val postId: Long) : AffectedPost()
        data class Answer(override val postId: Long) : AffectedPost()
    }
}