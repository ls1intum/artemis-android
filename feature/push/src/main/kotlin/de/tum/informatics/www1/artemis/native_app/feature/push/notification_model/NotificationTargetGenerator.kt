package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget.Companion.MESSAGE_NEW_REPLY
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.NotificationTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.UnknownNotificationTarget

/**
 * Utility class to generate notification targets directly from courseNotificationDTO
 * instead of relying on a separate target field.
 */
object NotificationTargetGenerator {

    /**
     * Generates a CommunicationPostTarget from courseNotificationDTO for communication notifications
     */
    fun generateCommunicationTarget(courseNotificationDTO: CourseNotificationDTO): CommunicationPostTarget {
        val params = courseNotificationDTO.parameters

        // For channel-related notifications, we might not have postId or channelId
        val postId = when (courseNotificationDTO.notificationType) {
            GeneralNotificationType.ADDED_TO_CHANNEL_NOTIFICATION -> 0L
            ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION -> params.replyId ?: 0L
            else -> params.postId ?: 0L
        }

        val conversationId = when (courseNotificationDTO.notificationType) {
            GeneralNotificationType.ADDED_TO_CHANNEL_NOTIFICATION -> params.channelId ?: 0L
            else -> params.channelId ?: 0L
        }

        return CommunicationPostTarget(
            message = "new-message",
            entity = "message",
            postId = postId,
            courseId = courseNotificationDTO.courseId,
            conversationId = conversationId
        )
    }

    /**
     * Generates an ExerciseTarget from courseNotificationDTO for exercise notifications
     */
    fun generateExerciseTarget(courseNotificationDTO: CourseNotificationDTO): ExerciseTarget {
        val params = courseNotificationDTO.parameters
        return ExerciseTarget(
            exerciseId = params.exerciseId ?: 0L,
            courseId = courseNotificationDTO.courseId
        )
    }

    /**
     * Generates the appropriate NotificationTarget based on the notification type
     */
    fun generateTarget(courseNotificationDTO: CourseNotificationDTO): NotificationTarget {
        return when (courseNotificationDTO.notificationType) {
            StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION,
            StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_NOTIFICATION,
            ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION,
            ReplyPostCommunicationNotificationType.NEW_MENTION_NOTIFICATION -> generateCommunicationTarget(courseNotificationDTO)

            GeneralNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION -> generateExerciseTarget(courseNotificationDTO)

            else -> UnknownNotificationTarget
        }
    }

    /**
     * Generates a target string for backward compatibility with existing code
     */
    fun generateTargetString(courseNotificationDTO: CourseNotificationDTO): String {
        val target = generateTarget(courseNotificationDTO)
        return when (target) {
            is CommunicationPostTarget -> {
                if (target.message == MESSAGE_NEW_REPLY) {
                    // Special format for channel notifications
                    """
                    {
                        "message": "new-reply",
                        "entity": "message", 
                        "mainPage": "courses",
                        "id": ${target.postId},
                        "course": ${target.courseId},
                        "conversation": ${target.conversationId}
                    }
                    """.trimIndent()
                } else {
                    // Regular communication target
                    """
                    {
                        "message": "new-message",
                        "entity": "message", 
                        "mainPage": "courses",
                        "id": ${target.postId},
                        "course": ${target.courseId},
                        "conversation": ${target.conversationId}
                    }
                    """.trimIndent()
                }
            }

            is ExerciseTarget -> """
            {
                "id": ${target.exerciseId},
                "course": ${target.courseId}
            }
            """.trimIndent()

            else -> "{}"
        }
    }
}