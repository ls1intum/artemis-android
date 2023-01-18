package de.tum.informatics.www1.artemis.native_app.core.datastore

import kotlinx.coroutines.flow.Flow
import javax.crypto.SecretKey

interface PushNotificationConfigurationService {

    val arePushNotificationEnabled: Flow<Boolean>

    suspend fun updateArePushNotificationEnabled(newIsEnabled: Boolean)

    suspend fun refreshAESKey(username: String, serverId: String): SecretKey

    /**
     * @return the current aes key or null if no key has been set yet.
     */
    suspend fun getCurrentAESKey(): SecretKey?
}