package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.reply

import android.content.Context
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.util.ForwardedSourcePostContent
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.datetime.Clock

class UpdateReplyNotificationWorker(
    appContext: Context,
    params: WorkerParameters,
    private val accountDataService: AccountDataService,
    private val communicationNotificationManager: CommunicationNotificationManager,
) : BaseCreatePostWorker(appContext, params) {

    override suspend fun doWork(
        courseId: Long,
        conversationId: Long,
        clientSidePostId: String,
        content: String,
        postType: PostType,
        forwardedSourcePostList: List<ForwardedSourcePostContent>,
        parentPostId: Long?
    ): Result {
        val accountData = accountDataService.getAccountData()

        return when (accountData) {
            is NetworkResponse.Response -> {
                val account = accountData.data

                communicationNotificationManager.addSelfMessage(
                    parentId = parentPostId ?: return Result.failure(),
                    authorLoginName = account.username ?: "self",
                    authorName = account.humanReadableName,
                    authorImageUrl = account.imageUrl,
                    body = content,
                    date = Clock.System.now()
                )

                Result.success()
            }

            is NetworkResponse.Failure -> Result.failure()
        }
    }
}
