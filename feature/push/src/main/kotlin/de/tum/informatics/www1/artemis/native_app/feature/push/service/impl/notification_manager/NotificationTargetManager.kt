package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.communicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CoursePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExercisePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.LecturePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.MetisTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.NotificationTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.UnknownNotificationTarget
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal object NotificationTargetManager {

    private val MainActivity =
        Class.forName("de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity")

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
                            "artemis://quiz_participation/${notificationTarget.courseId}/${notificationTarget.exerciseId}"
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
            is CoursePostTarget -> {
                "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/null/null"
            }

            is LecturePostTarget -> {
                "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/${notificationTarget.lectureId}/null"
            }

            is ExercisePostTarget -> {
                "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/${notificationTarget.exerciseId}/null/null"
            }

            is CommunicationPostTarget -> {
                "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/null/${notificationTarget.conversationId}"
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
            is CommunicationNotificationType -> getCommunicationNotificationTarget(type.communicationType, target)

            MiscNotificationType.QUIZ_EXERCISE_STARTED -> {
                Json.decodeFromString<ExerciseTarget>(target)
            }

            else -> UnknownNotificationTarget
        }
    }

    fun getCommunicationNotificationTarget(
        type: CommunicationType,
        target: String
    ): MetisTarget {
        return when (type) {
            CommunicationType.QNA_COURSE, CommunicationType.ANNOUNCEMENT -> {
                Json.decodeFromString<CoursePostTarget>(target)
            }

            CommunicationType.QNA_LECTURE -> {
                Json.decodeFromString<LecturePostTarget>(target)
            }

            CommunicationType.QNA_EXERCISE -> {
                Json.decodeFromString<ExercisePostTarget>(target)
            }
            CommunicationType.COMMUNICATION -> {
                Json.decodeFromString<CommunicationPostTarget>(target)
            }
        }
    }

    private fun buildOpenAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity),
        PendingIntent.FLAG_IMMUTABLE
    )
}