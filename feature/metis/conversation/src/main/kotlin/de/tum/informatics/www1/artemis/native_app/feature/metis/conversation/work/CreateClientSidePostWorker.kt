package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Worker that inserts a client post into the database.
 * This worker only fails when the input is wrong.
 */
class CreateClientSidePostWorker(
    appContext: Context,
    params: WorkerParameters,
    private val accountService: AccountService,
    private val accountDataService: AccountDataService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
) : BaseCreatePostWorker(appContext, params) {

    companion object {
        private const val TAG = "CreateClientSidePostWorker"
    }

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

        val authorAccount = accountDataService.getCachedAccountData(serverUrl, authToken)
            ?: Account() // Super edge case, just use nothing here.

        val author = User(
            username = authorAccount.username,
            name = authorAccount.name,
            id = authorAccount.id,
            firstName = authorAccount.firstName,
            lastName = authorAccount.lastName
        )

        when (postType) {
            PostType.POST -> {
                metisStorageService.insertClientSidePost(
                    host = serverConfigurationService.host.first(),
                    metisContext = MetisContext.Conversation(courseId, conversationId),
                    post = StandalonePost(
                        id = null,
                        title = null,
                        tags = null,
                        author = author,
                        authorRole = null, // We do not know the role of the user here!
                        content = content,
                        creationDate = Clock.System.now()
                    ),
                    clientSidePostId = clientSidePostId
                )
            }

            PostType.ANSWER_POST -> {
                metisStorageService.insertClientSidePost(
                    host = serverConfigurationService.host.first(),
                    metisContext = MetisContext.Conversation(courseId, conversationId),
                    post = AnswerPost(
                        id = null,
                        content = content,
                        author = author,
                        authorRole = null,
                        post = StandalonePost(id = parentPostId),
                        creationDate = Clock.System.now()
                    ),
                    clientSidePostId = clientSidePostId
                )
            }
        }

        Log.d(TAG, "Created client side post with id $clientSidePostId")

        return Result.success()
    }
}