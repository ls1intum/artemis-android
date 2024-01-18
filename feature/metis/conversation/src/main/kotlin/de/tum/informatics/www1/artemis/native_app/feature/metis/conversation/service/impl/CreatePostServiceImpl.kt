package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.tum.informatics.www1.artemis.native_app.core.common.defaultInternetWorkRequest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.CreateClientSidePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.SendConversationPostWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CreatePostServiceImpl(private val context: Context) : CreatePostService {

    override fun createPost(courseId: Long, conversationId: Long, content: String) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = null,
            clientSidePostId = null
        )
    }

    override fun createAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        content: String
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.ANSWER_POST,
            parentPostId = parentPostId,
            clientSidePostId = null
        )
    }

    override fun retryCreatePost(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = null,
            clientSidePostId = clientSidePostId
        )
    }

    override fun retryCreateAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        clientSidePostId: String,
        content: String
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = parentPostId,
            clientSidePostId = clientSidePostId
        )
    }

    override fun observeCreatePostWorkStatus(clientSidePostId: String): Flow<CreatePostService.Status> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(getWorkName(clientSidePostId))
            .map { info ->
                val failed =
                    info.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }
                val done = info.all { it.state == WorkInfo.State.SUCCEEDED }

                when {
                    failed -> CreatePostService.Status.FAILED
                    done -> CreatePostService.Status.FINISHED
                    else -> CreatePostService.Status.PENDING
                }
            }
    }

    private fun scheduleCreatePostWork(
        courseId: Long,
        conversationId: Long,
        content: String,
        postType: BaseCreatePostWorker.PostType,
        parentPostId: Long?,
        clientSidePostId: String?
    ) {
        val postId = clientSidePostId ?: UUID.randomUUID().toString()

        val inputData = BaseCreatePostWorker.createWorkInput(
            courseId = courseId,
            conversationId = conversationId,
            clientSidePostId = postId,
            content = content,
            postType = postType,
            parentPostId = parentPostId
        )

        val createPostRequest = OneTimeWorkRequestBuilder<CreateClientSidePostWorker>()
            .setInputData(inputData)
            .build()

        val sendPostRequest =
            defaultInternetWorkRequest<SendConversationPostWorker>(inputData = inputData)

        if (clientSidePostId == null) {
            WorkManager
                .getInstance(context)
                .beginUniqueWork(
                    getWorkName(postId),
                    ExistingWorkPolicy.REPLACE,
                    createPostRequest
                )
                .then(sendPostRequest)
                .enqueue()
        } else {
            WorkManager
                .getInstance(context)
                .beginUniqueWork(
                    getWorkName(clientSidePostId),
                    ExistingWorkPolicy.REPLACE,
                    sendPostRequest
                )
                .enqueue()
        }
    }

    private fun getWorkName(clientSidePostId: String) = "UploadPost-$clientSidePostId"
}