package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first

/**
 * Starts a new job to unsubscribe from all notifications for the current server.
 * After calling this method, we can logout or switch server.
 */
suspend fun unsubscribeFromNotifications(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    pushNotificationConfigurationService: PushNotificationConfigurationService,
    pushNotificationJobService: PushNotificationJobService
) {
    pushNotificationConfigurationService.firebaseToken.first()
        ?.let { firebaseToken ->
            pushNotificationJobService.scheduleUnsubscribeFromNotifications(
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first(),
                firebaseToken = firebaseToken
            )
        }
}