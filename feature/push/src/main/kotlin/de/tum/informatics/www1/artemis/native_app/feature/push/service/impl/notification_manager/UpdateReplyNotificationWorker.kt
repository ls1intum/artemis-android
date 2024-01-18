package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class UpdateReplyNotificationWorker(
    appContext: Context,
    params: WorkerParameters,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val accountDataService: AccountDataService,
    private val communicationNotificationManager: CommunicationNotificationManager,
) : BaseCreatePostWorker(appContext, params) {

    override suspend fun doWork(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String,
        postType: PostType,
        parentPostId: Long?
    ): Result {
        val serverUrl = serverConfigurationService.serverUrl.first()
        val authToken = accountService.authToken.first()

        // We can add to the notification that the user has responded. However, this does not have super high priority
        val accountData = accountDataService.getAccountData(
            serverUrl = serverUrl,
            bearerToken = authToken
        )

        return when (accountData) {
            is NetworkResponse.Response -> {
                val authorName =
                    "${accountData.data.firstName} ${accountData.data.lastName}"

                communicationNotificationManager.addSelfMessage(
                    parentId = conversationId,
                    type = CommunicationType.CONVERSATION,
                    authorName = authorName,
                    body = content,
                    date = Clock.System.now()
                )

                Result.success()
            }

            is NetworkResponse.Failure -> Result.failure()
        }
    }
}