package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.tum.informatics.www1.artemis.native_app.core.common.defaultInternetWorkRequest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.CreateClientSidePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.SendConversationPostWorker

class CreatePostServiceImpl(private val context: Context) : CreatePostService {
    override fun createPost(courseId: Long, conversationId: Long, content: String) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = null
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
            parentPostId = parentPostId
        )
    }

    private fun scheduleCreatePostWork(courseId: Long, conversationId: Long, content: String, postType: BaseCreatePostWorker.PostType, parentPostId: Long?) {
        val inputData = BaseCreatePostWorker.createWorkInput(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = postType,
            parentPostId = parentPostId
        )
        val createPostRequest = OneTimeWorkRequestBuilder<CreateClientSidePostWorker>()
            .setInputData(inputData)
            .build()

        val sendPostRequest =
            defaultInternetWorkRequest<SendConversationPostWorker>(inputData = inputData)

        WorkManager.getInstance(context)
            .beginWith(createPostRequest)
            .then(sendPostRequest)
            .enqueue()
    }
}