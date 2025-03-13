package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.runBlocking
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
        val pushCommunicationEntity = runBlocking { getCommunication(parentId) }
        onReceive(pushCommunicationEntity, context, intent)
    }

    private suspend fun getCommunication(parentId: Long): PushCommunicationEntity {
        val pushCommunicationDatabaseProvider: PushCommunicationDatabaseProvider = get()
        return pushCommunicationDatabaseProvider.pushCommunicationDao.getCommunication(parentId)
    }

    abstract fun onReceive(communicationEntity: PushCommunicationEntity, context: Context, intent: Intent)
}