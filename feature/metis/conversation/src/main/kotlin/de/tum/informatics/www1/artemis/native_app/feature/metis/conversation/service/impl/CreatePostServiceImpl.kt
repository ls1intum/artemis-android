package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.tum.informatics.www1.artemis.native_app.core.common.defaultInternetWorkRequest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostConfigurationBlock
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.CreatePostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.util.ForwardedSourcePostContent
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.CreateClientSidePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.SendConversationPostWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

internal class CreatePostServiceImpl(private val context: Context) : CreatePostService {

    override fun createPost(
        courseId: Long,
        conversationId: Long,
        content: String,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>?,
        configure: CreatePostConfigurationBlock
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = null,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = forwardedSourcePostList,
            clientSidePostId = null,
            configure = configure
        )
    }

    override fun createAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        content: String,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>?,
        configure: CreatePostConfigurationBlock
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.ANSWER_POST,
            parentPostId = parentPostId,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = forwardedSourcePostList,
            clientSidePostId = null,
            configure = configure
        )
    }

    override fun retryCreatePost(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>?,
        configure: CreatePostConfigurationBlock
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = null,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = forwardedSourcePostList,
            clientSidePostId = clientSidePostId,
            configure = configure
        )
    }

    override fun retryCreateAnswerPost(
        courseId: Long,
        conversationId: Long,
        parentPostId: Long,
        clientSidePostId: String,
        content: String,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>?,
        configure: CreatePostConfigurationBlock
    ) {
        scheduleCreatePostWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = BaseCreatePostWorker.PostType.POST,
            parentPostId = parentPostId,
            clientSidePostId = clientSidePostId,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = forwardedSourcePostList,
            configure = configure
        )
    }

    override fun observeCreatePostWorkStatus(clientSidePostId: String): Flow<CreatePostStatus> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(getWorkName(clientSidePostId))
            .map { info ->
                val failed =
                    info.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }
                val done = info.all { it.state == WorkInfo.State.SUCCEEDED }

                when {
                    failed -> CreatePostStatus.FAILED
                    done -> CreatePostStatus.FINISHED
                    else -> CreatePostStatus.PENDING
                }
            }
    }

    @SuppressLint("EnqueueWork")
    private fun scheduleCreatePostWork(
        courseId: Long,
        conversationId: Long,
        content: String,
        postType: BaseCreatePostWorker.PostType,
        parentPostId: Long?,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>?,
        clientSidePostId: String?,
        configure: CreatePostConfigurationBlock
    ) {
        val postId = clientSidePostId ?: UUID.randomUUID().toString()

        val inputData = BaseCreatePostWorker.createWorkInput(
            courseId = courseId,
            conversationId = conversationId,
            clientSidePostId = postId,
            content = content,
            postType = postType,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = forwardedSourcePostList,
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
                .let { configure(it, postId) }
                .enqueue()
        } else {
            WorkManager
                .getInstance(context)
                .beginUniqueWork(
                    getWorkName(clientSidePostId),
                    ExistingWorkPolicy.REPLACE,
                    sendPostRequest
                )
                .let { configure(it, postId) }
                .enqueue()
        }
    }

    private fun getWorkName(clientSidePostId: String) = "UploadPost-$clientSidePostId"
}