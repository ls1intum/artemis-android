package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.BroadcastReceiver
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class BaseCommunicationNotificationReceiver : BroadcastReceiver(), KoinComponent {

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