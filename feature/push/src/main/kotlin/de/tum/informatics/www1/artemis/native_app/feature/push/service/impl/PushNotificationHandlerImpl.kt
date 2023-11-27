package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextReporter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.communicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationTargetManager
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class PushNotificationHandlerImpl(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val supportedPushNotificationVersion: Int = PUSH_NOTIFICATION_VERSION
) : PushNotificationHandler {

    companion object {
        private const val TAG = "PushNotificationHandlerImpl"

        private val notificationJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        private const val PUSH_NOTIFICATION_VERSION = 1
    }

    override fun handleServerPushNotification(payload: String) {
        val version: PushNotificationVersion? = try {
            notificationJson.decodeFromString(payload)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

        when {
            version == null -> {
                Log.d(
                    TAG,
                    "Received push notification does not have a version we could read -> discarding"
                )
                return
            }

            version.version != supportedPushNotificationVersion -> {
                Log.d(
                    TAG,
                    "Received push notification has version ${version.version}, but we only support $supportedPushNotificationVersion -> discarding"
                )
                return
            }
        }

        val notification: ArtemisNotification<NotificationType> = try {
            notificationJson.decodeFromString(payload)
        } catch (e: SerializationException) {
            Log.d(TAG, "Cannot decode push notification -> discarding", e)
            return
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Cannot decode push notification -> discarding", e)
            return
        }

        // if the metis context this notification is about is already visible we do not pop that notification.
        val currentActivity = (context as? CurrentActivityListener)?.currentActivity?.value

        val visibleMetisContexts: List<VisibleMetisContext> =
            (currentActivity as? VisibleMetisContextReporter)?.visibleMetisContexts?.value.orEmpty()

        val notificationType = notification.type
        if (notificationType is CommunicationNotificationType) {
            val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
                notificationType.communicationType,
                notification.target
            )

            val visibleMetisContext =
                VisibleStandalonePostDetails(
                    metisTarget.metisContext,
                    metisTarget.postId
                )

            // If the context is already visible cancel now!
            if (visibleMetisContext in visibleMetisContexts) return
        }

        runBlocking {
            notificationManager.popNotification(
                context = context,
                artemisNotification = notification
            )
        }
    }

    @Serializable
    private data class PushNotificationVersion(val version: Int)
}
