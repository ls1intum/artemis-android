package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.delete

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DeleteNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val PARENT_ID = "parent_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val parentId = intent.getLongExtra(PARENT_ID, 0)

        val communicationNotificationManager: CommunicationNotificationManager = get()

        runBlocking {
            communicationNotificationManager.deleteCommunication(parentId)
        }
    }
}