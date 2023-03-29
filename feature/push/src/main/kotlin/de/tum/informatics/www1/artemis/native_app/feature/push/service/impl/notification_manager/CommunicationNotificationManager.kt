package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType

internal class CommunicationNotificationManager(private val context: Context) : BaseNotificationManager {

    fun popNotification(
        notificationType: CommunicationNotificationType,
        artemisNotification: ArtemisNotification
    ) {

    }
}