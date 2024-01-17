package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Worker that inserts a client post into the database.
 * This worker only fails when the input is wrong.
 */
class CreateClientSidePostWorker(
    appContext: Context,
    params: WorkerParameters,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val courseId: Long = inputData.getLong(SendConversationPostWorker.KEY_COURSE_ID, 0)
        val conversationId: Long =
            inputData.getLong(SendConversationPostWorker.KEY_CONVERSATION_ID, 0)
        val content =
            inputData.getString(SendConversationPostWorker.KEY_CONTENT) ?: return Result.failure()
        val parentPostId = inputData.getLong(SendConversationPostWorker.KEY_PARENT_POST_ID, 0)

        val postType =
            SendConversationPostWorker.Companion.PostType.valueOf(
                inputData.getString(
                    SendConversationPostWorker.KEY_POST_TYPE
                ) ?: return Result.failure()
            )

        val author = User(username = "TODO", id = 0L)
        val userRole = UserRole.INSTRUCTOR

        when (postType) {
            SendConversationPostWorker.Companion.PostType.POST -> {
                metisStorageService.insertClientSidePost(
                    host = serverConfigurationService.host.first(),
                    metisContext = MetisContext.Conversation(courseId, conversationId),
                    post = StandalonePost(
                        id = null,
                        title = null,
                        tags = null,
                        author = author,
                        authorRole = userRole,
                        content = content,
                        creationDate = Clock.System.now()
                    )
                )
            }

            SendConversationPostWorker.Companion.PostType.ANSWER_POST -> {
                metisStorageService.insertClientSidePost(
                    host = serverConfigurationService.host.first(),
                    metisContext = MetisContext.Conversation(courseId, conversationId),
                    post = AnswerPost(
                        id = null,
                        content = content,
                        author = author,
                        authorRole = userRole,
                        post = StandalonePost(id = parentPostId),
                        creationDate = Clock.System.now()
                    )
                )
            }
        }

        return Result.success()
    }
}