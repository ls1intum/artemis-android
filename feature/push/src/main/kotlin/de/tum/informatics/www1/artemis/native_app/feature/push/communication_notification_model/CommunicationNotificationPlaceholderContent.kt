package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import kotlinx.datetime.Instant

private interface NotificationPlaceholderContentFactory {
    fun fromNotificationPlaceholders(notificationPlaceholders: List<String>): CommunicationNotificationPlaceholderContent
}


/**
 * The important information of a notification is sent not in json so it can not easily be
 * deserialized. It is only available as a list of strings called "notificationPlaceholder".
 *
 * This class is used to parse this list into a structured object.
 */
sealed class CommunicationNotificationPlaceholderContent {

    abstract val courseTitle: String
    abstract val postContent: String
    abstract val postAuthor: String
    abstract val containerTitle: String

    fun toCommunicationMessageEntity(
        parentId: Long,
        date: Instant
    ): CommunicationMessageEntity {
        val (relevantContent, relevantAuthor) = when (this) {
            is StandalonePost -> {
                postContent to postAuthor
            }

            is ReplyPost -> {
                answerPostContent to answerPostAuthor
            }
        }

        return CommunicationMessageEntity(
            communicationParentId = parentId,
            authorName = relevantAuthor,
            text = relevantContent,
            date = date
        )
    }

    companion object {
        fun fromArtemisNotification(
            notification: ArtemisNotification<CommunicationNotificationType>
        ): CommunicationNotificationPlaceholderContent {
            return when (notification.type) {
                is StandalonePostCommunicationNotificationType ->
                    StandalonePost.fromNotificationPlaceholders(notification.notificationPlaceholders)
                is ReplyPostCommunicationNotificationType ->
                    ReplyPost.fromNotificationPlaceholders(notification.notificationPlaceholders)
            }
        }
    }

    data class StandalonePost(
        override val courseTitle: String,
        override val postContent: String,
        val conversationType: ConversationType,
        override val postAuthor: String,
        override val containerTitle: String,
    ) : CommunicationNotificationPlaceholderContent() {

        companion object : NotificationPlaceholderContentFactory {
            override fun fromNotificationPlaceholders(notificationPlaceholders: List<String>): CommunicationNotificationPlaceholderContent {
                val conversationType = ConversationType.valueOf(notificationPlaceholders[5])

                return StandalonePost(
                    courseTitle = notificationPlaceholders[0],
                    postContent = notificationPlaceholders[1],
                    conversationType = conversationType,
                    postAuthor = when (conversationType) {
                        ConversationType.CHANNEL -> notificationPlaceholders[4]
                        else -> notificationPlaceholders[3]
                    },
                    containerTitle = when (conversationType) {
                        ConversationType.CHANNEL -> notificationPlaceholders[3]
                        else -> notificationPlaceholders[4]
                    }
                )
            }
        }

        enum class ConversationType {
            CHANNEL,
            GROUP_CHAT,
            ONE_TO_ONE_CHAT
        }
    }

    data class ReplyPost(
        override val courseTitle: String,
        override val postContent: String,
        override val postAuthor: String,
        override val containerTitle: String,
        val answerPostContent: String,
        val answerPostAuthor: String,
    ) : CommunicationNotificationPlaceholderContent() {

        companion object : NotificationPlaceholderContentFactory {
            override fun fromNotificationPlaceholders(notificationPlaceholders: List<String>): CommunicationNotificationPlaceholderContent {
                return ReplyPost(
                    courseTitle = notificationPlaceholders[0],
                    postContent = notificationPlaceholders[1],
                    postAuthor = notificationPlaceholders[3],
                    containerTitle = notificationPlaceholders[7],
                    answerPostContent = notificationPlaceholders[4],
                    answerPostAuthor = notificationPlaceholders[6]
                )
            }
        }
    }
}