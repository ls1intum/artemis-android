package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Worker that uploads a post or answer post to the Artemis server.
 * If the input is invalid, the worker fails.
 * If the input is valid, but the reply could not be uploaded, the worker will schedule a retry.
 * If the upload failed 5 times, the worker will fail and pop a notification to notify the user about the failure.
 */
class SendConversationPostWorker(
    appContext: Context,
    params: WorkerParameters,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val conversationService: ConversationService
) : BaseCreatePostWorker(appContext, params) {

    companion object {
        /**
         * Id of the post created on client side
         */
        const val KEY_CLIENT_SIDE_POST_ID = "client_side_post_id"


        private const val TAG = "ReplyWorker"
    }

    override suspend fun doWork(
        courseId: Long,
        conversationId: Long,
        content: String,
        postType: PostType,
        parentPostId: Long?
    ): Result {
        val clientSidePostId =
            inputData.getString(KEY_CLIENT_SIDE_POST_ID) ?: return Result.failure()

        Log.d(TAG, "Starting send post to server. ClientSidePostId=$clientSidePostId")

        val getErrorReturnType: suspend () -> Result = if (runAttemptCount > 5) {
            {
                popFailureNotification(postType, content)
                Result.failure()
            }
        } else {
            { Result.retry() }
        }

        return when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> {
                val serverUrl = serverConfigurationService.serverUrl.first()
                val host = serverConfigurationService.host.first()
                val metisContext = MetisContext.Conversation(courseId, conversationId)

                when (postType) {
                    PostType.POST -> {
                        uploadPost(
                            courseId = courseId,
                            conversationId = conversationId,
                            content = content,
                            serverUrl = serverUrl,
                            authToken = authData.authToken
                        ).onSuccess { post ->
                            metisStorageService.upgradeClientSidePost(
                                host = host,
                                metisContext = metisContext,
                                clientSidePostId = clientSidePostId,
                                post = post
                            )
                        }
                    }

                    PostType.ANSWER_POST -> {
                        // Must not be null!
                        if (parentPostId == null) return Result.failure()

                        uploadAnswerPost(
                            courseId = courseId,
                            conversationId = conversationId,
                            postId = parentPostId,
                            content = content,
                            serverUrl = serverUrl,
                            authToken = authData.authToken
                        ).onSuccess { post ->
                            metisStorageService.upgradeClientSideAnswerPost(
                                host = host,
                                metisContext = metisContext,
                                clientSidePostId = clientSidePostId,
                                post = post
                            )
                        }
                    }
                }.map(mapSuccess = { Result.success() }, mapFailure = { getErrorReturnType() })
            }

            AccountService.AuthenticationData.NotLoggedIn -> getErrorReturnType()
        }
    }

    private suspend fun uploadPost(
        courseId: Long,
        conversationId: Long,
        content: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return loadConversation(
            courseId = courseId,
            conversationId = conversationId,
            authToken = authToken,
            serverUrl = serverUrl
        ).then { conversation ->
            metisModificationService.createPost(
                context = MetisContext.Conversation(courseId, conversationId),
                post = StandalonePost(
                    id = null,
                    title = null,
                    tags = null,
                    content = content,
                    conversation = conversation,
                    creationDate = Clock.System.now(),
                    displayPriority = DisplayPriority.NONE
                ),
                serverUrl = serverUrl,
                authToken = authToken
            )
        }
    }

    private suspend fun uploadAnswerPost(
        courseId: Long,
        conversationId: Long,
        postId: Long,
        content: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> {
        return loadConversation(
            courseId = courseId,
            conversationId = conversationId,
            authToken = authToken,
            serverUrl = serverUrl
        ).then { conversation ->
            metisModificationService.createAnswerPost(
                context = MetisContext.Conversation(courseId, conversationId),
                post = AnswerPost(
                    content = content,
                    post = StandalonePost(
                        id = postId,
                        conversation = conversation
                    ),
                    creationDate = Clock.System.now()
                ),
                serverUrl = serverUrl,
                authToken = authToken
            )
        }
    }

    private suspend fun loadConversation(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Conversation> =
        conversationService.getConversation(
            courseId = courseId,
            conversationId = conversationId,
            authToken = authToken,
            serverUrl = serverUrl
        )

    private suspend fun popFailureNotification(type: PostType, content: String) {
        val id = ArtemisNotificationManager.getNextNotificationId(applicationContext)

        val title = when (type) {
            PostType.POST -> R.string.push_notification_send_post_failed_title
            PostType.ANSWER_POST -> R.string.push_notification_send_answer_post_failed_title
        }

        val message = when (type) {
            PostType.POST -> R.string.push_notification_send_post_failed_message
            PostType.ANSWER_POST -> R.string.push_notification_send_answer_post_failed_message
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                ArtemisNotificationChannel.MiscNotificationChannel.id
            )
                .setSmallIcon(R.drawable.baseline_sms_failed_24)
                .setContentTitle(applicationContext.getString(title))
                .setContentText(
                    applicationContext.getString(
                        message,
                        content
                    )
                )
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat
                .from(applicationContext)
                .notify(id, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not push reply notification due to missing permission")
        }
    }
}