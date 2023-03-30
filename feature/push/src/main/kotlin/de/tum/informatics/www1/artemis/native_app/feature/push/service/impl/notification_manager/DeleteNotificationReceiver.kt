package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DeleteNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val PARENT_ID = "parent_id"
        const val COMMUNICATION_TYPE = "communication_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val parentId = intent.getLongExtra(PARENT_ID, 0)
        val communicationType =
            CommunicationType.valueOf(intent.getStringExtra(COMMUNICATION_TYPE) ?: return)

        val communicationNotificationManager: CommunicationNotificationManager = get()

        runBlocking {
            communicationNotificationManager.deleteCommunication(parentId, communicationType)
        }
    }
}