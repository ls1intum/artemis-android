package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage

import kotlinx.coroutines.flow.Flow

/** A revision distinguishes a newer draft even when its body happens to match an older send. */
data class StoredReplyDraft(val text: String, val revision: String? = null)

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

    fun observeStoredReplyDraft(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?
    ): Flow<StoredReplyDraft>

    suspend fun updateStoredReplyDraft(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?,
        draft: StoredReplyDraft
    )

    /** Atomically clears only the exact revision accepted by the send and returns the surviving draft. */
    suspend fun clearStoredReplyDraftAfterSend(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?,
        sentDraft: StoredReplyDraft
    ): StoredReplyDraft
}
