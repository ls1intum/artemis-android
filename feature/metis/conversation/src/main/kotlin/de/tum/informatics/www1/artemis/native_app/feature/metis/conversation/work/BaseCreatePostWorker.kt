package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.ForwardedSourcePostContent
import kotlinx.serialization.json.Json

abstract class BaseCreatePostWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_POST_TYPE = "type"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_CONVERSATION_ID = "conversation_id"
        const val KEY_CONTENT = "content"
        const val KEY_HAS_FORWARDED_MESSAGE = "has_forwarded_message"
        const val KEY_FORWARDED_SOURCE_POST_LIST = "forwarded_source_post_list"
        const val KEY_PARENT_POST_ID = "parent_post_id"

        /**
         * Id of the post created on client side
         */
        const val KEY_CLIENT_SIDE_POST_ID = "client_side_post_id"

        fun createWorkInput(
            courseId: Long,
            conversationId: Long,
            clientSidePostId: String,
            content: String,
            postType: PostType,
            hasForwardedMessage: Boolean,
            forwardedSourcePostList: List<ForwardedSourcePostContent>? = null,
            parentPostId: Long?
        ): Data {
            val encodedSourcePostsList = Json.encodeToString(forwardedSourcePostList ?: emptyList())

            return Data.Builder()
                .putLong(KEY_COURSE_ID, courseId)
                .putLong(KEY_CONVERSATION_ID, conversationId)
                .putString(KEY_CLIENT_SIDE_POST_ID, clientSidePostId)
                .putString(KEY_CONTENT, content)
                .putString(KEY_POST_TYPE, postType.name)
                .putBoolean(KEY_HAS_FORWARDED_MESSAGE, hasForwardedMessage)
                .putString(KEY_FORWARDED_SOURCE_POST_LIST, encodedSourcePostsList)
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

        val hasForwardedMessage = inputData.getBoolean(KEY_HAS_FORWARDED_MESSAGE, false)

        val decodedSourcePostList = inputData.getString(KEY_FORWARDED_SOURCE_POST_LIST) ?: "[]"
        val sourcePosts = Json.decodeFromString<List<ForwardedSourcePostContent>?>(decodedSourcePostList)

        val parentPostId = if (KEY_PARENT_POST_ID in inputData.keyValueMap)
            inputData.getLong(KEY_PARENT_POST_ID, 0)
        else null

        val clientSidePostId =
            inputData.getString(KEY_CLIENT_SIDE_POST_ID) ?: return Result.failure()

        return doWork(
            courseId = courseId,
            conversationId = conversationId,
            clientSidePostId = clientSidePostId,
            content = content,
            postType = postType,
            hasForwardedMessage = hasForwardedMessage,
            forwardedSourcePostList = sourcePosts,
            parentPostId = parentPostId
        )
    }

    abstract suspend fun doWork(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String,
        postType: PostType,
        hasForwardedMessage: Boolean,
        forwardedSourcePostList: List<ForwardedSourcePostContent>? = null,
        parentPostId: Long?
    ): Result

    enum class PostType {
        POST,
        ANSWER_POST
    }
}
