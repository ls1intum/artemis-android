package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service

import kotlinx.coroutines.flow.Flow

interface CreatePostService {

    fun createPost(courseId: Long, conversationId: Long, content: String)

    fun retryCreatePost(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String
    )

    fun createAnswerPost(courseId: Long, conversationId: Long, parentPostId: Long, content: String)

    fun retryCreateAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        clientSidePostId: String,
        content: String
    )

    fun observeCreatePostWorkStatus(clientSidePostId: String): Flow<Status>

    enum class Status {
        PENDING,
        FAILED,
        FINISHED
    }
}