package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service

import androidx.work.WorkContinuation
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.CreatePostStatus
import kotlinx.coroutines.flow.Flow

typealias CreatePostConfigurationBlock = WorkContinuation.(clientSidePostId: String) -> WorkContinuation

interface CreatePostService {

    fun createPost(
        courseId: Long,
        conversationId: Long,
        content: String,
        configure: CreatePostConfigurationBlock = { this }
    )

    fun retryCreatePost(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String,
        configure: CreatePostConfigurationBlock = { this }
    )

    fun createAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        content: String,
        configure: CreatePostConfigurationBlock = { this }
    )

    fun retryCreateAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        clientSidePostId: String,
        content: String,
        configure: CreatePostConfigurationBlock = { this }
    )

    fun observeCreatePostWorkStatus(clientSidePostId: String): Flow<CreatePostStatus>
}
