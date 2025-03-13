package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class BaseCommunicationNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        /** The id of the communication entity that this notification belongs to. */
        const val PARENT_ID = "parent_id"
    }

    protected val communicationNotificationManager: CommunicationNotificationManager = get()

    override fun onReceive(context: Context, intent: Intent) {
        val parentId = intent.getLongExtra(PARENT_ID, 0)
        onReceive(parentId, context, intent)
    }

    abstract fun onReceive(parentId: Long, context: Context, intent: Intent)



    protected suspend fun getMetisContext(parentId: Long): MetisContext.Conversation {
        return getMetisContextAndPostId(parentId).first
    }

    protected suspend fun getMetisContextAndPostId(parentId: Long): Pair<MetisContext.Conversation, Long> {
        val pushCommunicationDatabaseProvider: PushCommunicationDatabaseProvider = get()
        return pushCommunicationDatabaseProvider.database.withTransaction {
            val communication = pushCommunicationDatabaseProvider.pushCommunicationDao.getCommunication(parentId)

            val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
                communication.target
            )

            metisTarget.metisContext to metisTarget.postId
        }
    }
}