package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CourseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CourseNotificationDTO
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.GeneralNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationTargetGenerator
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.getNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.MetisTarget

internal object NotificationTargetManager {

    private val MainActivity
        get() = Class.forName("de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity")

    fun buildOnClickIntent(
        context: Context,
        notification: ArtemisNotification<*>
    ): PendingIntent {
        try {
            val uriString: String? =
                when (val notificationTarget =
                    NotificationTargetGenerator.generateTarget(notification.courseNotificationDTO)) {
                    is MetisTarget -> getMetisDeeplink(notificationTarget)

                    is ExerciseTarget -> {
                        if (notification.getNotificationType() == GeneralNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION) {
                            ExerciseDeeplinks.ToQuizParticipation.inAppLink(
                                notificationTarget.courseId,
                                notificationTarget.exerciseId
                            )
                        } else {
                            ExerciseDeeplinks.ToExercise.inAppLink(
                                notificationTarget.courseId,
                                notificationTarget.exerciseId
                            )
                        }
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
                if (notificationTarget.postId == 0L) {
                    if (notificationTarget.conversationId == 0L) {
                        " " //In case remove from channel/channel deleted
                    } else {
                        CommunicationDeeplinks.ToConversation.inAppLink(
                            notificationTarget.courseId,
                            notificationTarget.conversationId
                        )
                    }
                } else {
                    CommunicationDeeplinks.ToPostById.inAppLink(
                        notificationTarget.courseId,
                        notificationTarget.conversationId,
                        notificationTarget.postId
                    )
                }
            }
        }
    }

    fun getMetisContentIntent(context: Context, notificationTarget: MetisTarget): PendingIntent =
        getDeeplinkIntent(
            context,
            getMetisDeeplink(notificationTarget)
        )

    private fun getDeeplinkIntent(context: Context, deeplink: String): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(deeplink)
            ),
            PendingIntent.FLAG_IMMUTABLE
        )

    fun getCommunicationNotificationTarget(
        courseNotificationDTO: CourseNotificationDTO
    ): CommunicationPostTarget {
        return NotificationTargetGenerator.generateCommunicationTarget(courseNotificationDTO)
    }

    fun CommunicationPostTarget.toJsonString(): String {
        return """
        {
            "message": "new-message",
            "entity": "message", 
            "mainPage": "courses",
            "id": $postId,
            "course": $courseId,
            "conversation": $conversationId
        }
        """.trimIndent()
    }

    private fun buildOpenAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity),
        PendingIntent.FLAG_IMMUTABLE
    )
}