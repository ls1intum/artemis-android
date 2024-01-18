package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters

abstract class BaseCreatePostWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_POST_TYPE = "type"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_CONVERSATION_ID = "conversation_id"
        const val KEY_CONTENT = "content"
        const val KEY_PARENT_POST_ID = "parent_post_id"


        fun createWorkInput(
            courseId: Long,
            conversationId: Long,
            content: String,
            postType: PostType,
            parentPostId: Long?
        ): Data {
            return Data.Builder()
                .putLong(KEY_COURSE_ID, courseId)
                .putLong(KEY_CONVERSATION_ID, conversationId)
                .putString(KEY_CONTENT, content)
                .putString(KEY_POST_TYPE, postType.name)
                .apply {
                    if (parentPostId != null) putLong(KEY_PARENT_POST_ID, parentPostId)
                }
                .build()
        }
    }

    final override suspend fun doWork(): Result {
        val courseId: Long = inputData.getLong(KEY_COURSE_ID, 0)
        val conversationId: Long =
            inputData.getLong(KEY_CONVERSATION_ID, 0)
        val content =
            inputData.getString(KEY_CONTENT) ?: return Result.failure()

        val postType = BaseCreatePostWorker.PostType.valueOf(
            inputData.getString(KEY_POST_TYPE) ?: return Result.failure()
        )

        val parentPostId = if (KEY_PARENT_POST_ID in inputData.keyValueMap)
            inputData.getLong(KEY_PARENT_POST_ID, 0)
        else null

        return doWork(
            courseId = courseId,
            conversationId = conversationId,
            content = content,
            postType = postType,
            parentPostId = parentPostId
        )
    }

    abstract suspend fun doWork(
        courseId: Long,
        conversationId: Long,
        content: String,
        postType: PostType,
        parentPostId: Long?
    ): Result

    enum class PostType {
        POST,
        ANSWER_POST
    }
}