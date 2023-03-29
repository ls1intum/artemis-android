package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CoursePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExercisePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.LecturePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.MetisTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.NotificationTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
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
                    is CoursePostTarget -> {
                        "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/null"
                    }

                    is LecturePostTarget -> {
                        "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/${notificationTarget.lectureId}"
                    }

                    is ExercisePostTarget -> {
                        "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/${notificationTarget.exerciseId}/null"
                    }

                    is ExerciseTarget -> {
                        if (type == MiscNotificationType.QUIZ_EXERCISE_STARTED) {
                            "artemis://quiz_participation/${notificationTarget.courseId}/${notificationTarget.exerciseId}"
                        } else null
                    }

                    else -> null
                }

            return if (uriString != null) {
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uriString)
                    ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else buildOpenAppIntent(context)
        } catch (e: Exception) {
            return buildOpenAppIntent(context)
        }
    }

    private fun getNotificationTarget(type: NotificationType, target: String): NotificationTarget {
        return when (type) {
            is CommunicationNotificationType -> getCommunicationNotificationTarget(type, target)

            MiscNotificationType.QUIZ_EXERCISE_STARTED -> {
                Json.decodeFromString<ExerciseTarget>(target)
            }

            else -> UnknownNotificationTarget
        }
    }

    fun getCommunicationNotificationTarget(
        type: CommunicationNotificationType,
        target: String
    ): MetisTarget {
        return when (type) {
            ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_COURSE_POST, StandalonePostCommunicationNotificationType.NEW_COURSE_POST, StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_POST -> {
                Json.decodeFromString<CoursePostTarget>(target)
            }

            StandalonePostCommunicationNotificationType.NEW_LECTURE_POST, ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_LECTURE_POST -> {
                Json.decodeFromString<LecturePostTarget>(target)
            }

            StandalonePostCommunicationNotificationType.NEW_EXERCISE_POST, ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_EXERCISE_POST -> {
                Json.decodeFromString<ExercisePostTarget>(target)
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