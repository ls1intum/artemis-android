package de.tum.informatics.www1.artemis.native_app.feature.push.service

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType

interface NotificationManager {

    suspend fun popNotification(
        context: Context,
        artemisNotification: ArtemisNotification<*>
    )
}