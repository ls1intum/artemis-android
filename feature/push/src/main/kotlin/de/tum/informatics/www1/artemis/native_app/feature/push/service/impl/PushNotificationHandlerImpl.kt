package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextReporter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.getNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
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

        /// The version is 1, as of Artemis 6.6.7.
        ///
        /// The version is declared in the constants of Artemis, see [source](https://github.com/ls1intum/Artemis/blob/6.6.7/src/main/java/de/tum/in/www1/artemis/config/Constants.java#L318).
        private const val PUSH_NOTIFICATION_VERSION = 1

        internal fun isNotificationContextInVisibleMetisContexts(
            notification: ArtemisNotification<out NotificationType>,
            visibleMetisContexts: List<VisibleMetisContext>
        ): Boolean {
            val notificationType = notification.getNotificationType()
            if (notificationType !is CommunicationNotificationType) return false

            val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
                courseNotificationDTO = notification.courseNotificationDTO
            )

            val visibleMetisContext = if (notificationType is ReplyPostCommunicationNotificationType) {
                VisibleStandalonePostDetails(
                    metisContext = metisTarget.metisContext,
                    postId = metisTarget.postId
                )
            } else {
                VisiblePostList(metisContext = metisTarget.metisContext)
            }

            return visibleMetisContext in visibleMetisContexts
        }
    }

    override fun handleServerPushNotification(payload: String) {
        if (!validatePushNotificationVersion(payload)) return

        val notification = decodeNotification(payload) ?: return

        if (isNotificationContextAlreadyObservedByUser(notification)) {
            Log.d(TAG, "Notification context is already observed by user -> discarding")
            return
        }

        runBlocking {
            notificationManager.popNotification(
                context = context,
                artemisNotification = notification
            )
        }
    }

    private fun validatePushNotificationVersion(payload: String): Boolean {
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
                return false
            }

            version.version != supportedPushNotificationVersion -> {
                Log.d(
                    TAG,
                    "Received push notification has version ${version.version}, but we only support $supportedPushNotificationVersion -> discarding"
                )
                return false
            }
        }
        return true
    }

    private fun decodeNotification(payload: String): ArtemisNotification<NotificationType>? {
        return try {
            notificationJson.decodeFromString(payload)
        } catch (e: SerializationException) {
            Log.d(TAG, "Cannot decode push notification -> discarding", e)
            return null
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Cannot decode push notification -> discarding", e)
            return null
        }
    }

    private fun isNotificationContextAlreadyObservedByUser(notification: ArtemisNotification<NotificationType>): Boolean {
        val currentActivity = (context as? CurrentActivityListener)?.currentActivity?.value ?: return false

        val visibleMetisContexts: List<VisibleMetisContext> =
            (currentActivity as? VisibleMetisContextReporter)?.visibleMetisContexts?.value ?: return false

        return isNotificationContextInVisibleMetisContexts(notification, visibleMetisContexts)
    }


    @Serializable
    private data class PushNotificationVersion(val version: Int)
}
