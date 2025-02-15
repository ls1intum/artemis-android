package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first

/**
 * Starts a new job to unsubscribe from all notifications for the current server.
 * After calling this method, we can logout or switch server.
 */
suspend fun unsubscribeFromNotifications(
    pushNotificationConfigurationService: PushNotificationConfigurationService,
    pushNotificationJobService: PushNotificationJobService
) {
    pushNotificationConfigurationService.firebaseToken.first()
        ?.let { firebaseToken ->
            pushNotificationJobService.scheduleUnsubscribeFromNotifications(
                firebaseToken = firebaseToken
            )
        }
}