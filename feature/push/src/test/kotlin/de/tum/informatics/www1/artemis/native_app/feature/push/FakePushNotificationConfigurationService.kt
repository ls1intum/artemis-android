package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.crypto.SecretKey

class FakePushNotificationConfigurationService : PushNotificationConfigurationService {

    override val firebaseToken: Flow<String?> = flowOf("Fake firebase token")

    override fun getArePushNotificationsEnabledFlow(serverUrl: String): Flow<Boolean> = flowOf(true)

    override suspend fun updateArePushNotificationEnabled(
        newIsEnabled: Boolean,
        serverUrl: String,
        authToken: String
    ): Boolean = true

    override suspend fun getCurrentAESKey(): SecretKey = throw NotImplementedError()

    override suspend fun storeAESKey(key: SecretKey) = throw NotImplementedError()

    override suspend fun storeFirebaseToken(token: String?) = throw NotImplementedError()
}
