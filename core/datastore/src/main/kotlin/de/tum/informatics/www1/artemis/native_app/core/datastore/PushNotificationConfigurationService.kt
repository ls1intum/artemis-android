package de.tum.informatics.www1.artemis.native_app.core.datastore

import kotlinx.coroutines.flow.Flow

interface PushNotificationConfigurationService {

    val arePushNotificationEnabled: Flow<Boolean>

    suspend fun updateArePushNotificationEnabled(newIsEnabled: Boolean)
}