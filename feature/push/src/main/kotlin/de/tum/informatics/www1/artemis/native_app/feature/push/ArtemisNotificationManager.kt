package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Handles the notifications.
 * To get the next notification id use getNextNotificationId.
 */
object ArtemisNotificationManager {

    private val LatestPushNotificationId = intPreferencesKey("latestPushNotificationId")

    private val Context.notificationDataStore by preferencesDataStore("push_notification_ids")

    suspend fun getNextNotificationId(context: Context): Int {
        val id =
            context.notificationDataStore.data.map { it[LatestPushNotificationId] ?: 0 }.first() + 1

        context.notificationDataStore.edit { data ->
            data[LatestPushNotificationId] = id
        }

        return id
    }
}
