package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mark_as_read

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarkConversationAsReadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val artemisContextProvider: ArtemisContextProvider,
    private val conversationService: ConversationService,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_COURSE_ID = "course_id"
        const val KEY_CONVERSATION_ID = "conversation_id"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val courseId = inputData.getLong(KEY_COURSE_ID, -1)
            val conversationId = inputData.getLong(KEY_CONVERSATION_ID, -1)

            if (courseId == -1L || conversationId == -1L) {
                return@withContext Result.failure()
            }

            val artemisContext = artemisContextProvider.stateFlow.value
            conversationService.markConversationAsRead(
                courseId = courseId,
                conversationId = conversationId,
                authToken = artemisContext.authTokenOrEmptyString,
                serverUrl = artemisContext.serverUrl
            )

            Result.success()
        }
    }
}