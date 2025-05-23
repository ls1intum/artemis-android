package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.MetisTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.NotificationTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.UnknownNotificationTarget
import kotlinx.serialization.json.Json

internal object NotificationTargetManager {

    private val MainActivity
        get() = Class.forName("de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun buildOnClickIntent(
        context: Context,
        type: NotificationType,
        target: String
    ): PendingIntent {
        try {
            val uriString: String? =
                when (val notificationTarget = getNotificationTarget(type, target)) {
                    is MetisTarget -> getMetisDeeplink(notificationTarget)

                    is ExerciseTarget -> {
                        if (type == MiscNotificationType.QUIZ_EXERCISE_STARTED) {
                            ExerciseDeeplinks.ToQuizParticipation.inAppLink(
                                notificationTarget.courseId,
                                notificationTarget.exerciseId
                            )
                        } else null
                    }

                    else -> null
                }

            return if (uriString != null) {
                getDeeplinkIntent(context, uriString)
            } else buildOpenAppIntent(context)
        } catch (e: Exception) {
            return buildOpenAppIntent(context)
        }
    }

    private fun getMetisDeeplink(notificationTarget: MetisTarget): String {
        return when (notificationTarget) {
            is CommunicationPostTarget -> {
                CommunicationDeeplinks.ToPostById.inAppLink(
                    notificationTarget.courseId,
                    notificationTarget.conversationId,
                    notificationTarget.postId
                )
            }
        }
    }

    fun getMetisContentIntent(context: Context, notificationTarget: MetisTarget): PendingIntent = getDeeplinkIntent(
        context,
        getMetisDeeplink(notificationTarget)
    )

    private fun getDeeplinkIntent(context: Context, deeplink: String): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(deeplink)
        ),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun getNotificationTarget(type: NotificationType, target: String): NotificationTarget {
        return when (type) {
            is CommunicationNotificationType -> getCommunicationNotificationTarget(target)

            MiscNotificationType.QUIZ_EXERCISE_STARTED -> {
                json.decodeFromString<ExerciseTarget>(target)
            }

            else -> UnknownNotificationTarget
        }
    }

    fun getCommunicationNotificationTarget(
        target: String
    ): CommunicationPostTarget {
        return json.decodeFromString<CommunicationPostTarget>(target)
    }

    fun CommunicationPostTarget.toJsonString(): String {
        return json.encodeToString(CommunicationPostTarget.serializer(), this)
    }

    private fun buildOpenAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity),
        PendingIntent.FLAG_IMMUTABLE
    )
}