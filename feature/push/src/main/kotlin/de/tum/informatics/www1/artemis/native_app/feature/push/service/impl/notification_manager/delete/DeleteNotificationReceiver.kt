package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.delete

import android.content.Context
import android.content.Intent
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.BaseCommunicationNotificationReceiver
import kotlinx.coroutines.runBlocking

class DeleteNotificationReceiver : BaseCommunicationNotificationReceiver() {

    override fun onReceive(
        communicationEntity: PushCommunicationEntity,
        context: Context,
        intent: Intent
    ) {
        runBlocking {
            communicationNotificationManager.deleteCommunication(communicationEntity.parentId)
        }
    }
}