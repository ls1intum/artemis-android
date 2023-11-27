package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage

interface ReplyTextStorageService {

    suspend fun getStoredReplyText(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?
    ): String

    suspend fun updateStoredReplyText(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?,
        text: String
    )
}
