package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType


// The code in this file is heavily inspired by the iOS implementation.
// See here: https://github.com/ls1intum/artemis-ios-core-modules/blob/main/Sources/PushNotifications/Models/PushNotification%2BCommunication.swift
private const val TAG = "CommunicationNotificationPlaceholderContent"

/**
 * The important information of a notification is sent not in json so it can not easily be
 * deserialized. It is only available as a list of strings called "notificationPlaceholder".
 *
 * This class is used to parse the relevant information of this list into a structured object.
 */
data class CommunicationNotificationPlaceholderContent(
    val authorName: String,
    val channelName: String,
    val courseName: String,
    val userId: String,
    val messageId: String,
    val profilePicUrl: String?,
    val messageContent: String,
    val type: ConversationType?,
    val isReply: Boolean
) {
    enum class ConversationType(val rawValue: String) {
        CHANNEL("channel"),
        ONE_TO_ONE_CHAT("oneToOneChat"),
        GROUP_CHAT("groupChat");

        companion object {
            fun fromString(rawValue: String): ConversationType =
                entries.first { it.rawValue == rawValue }
        }
    }

    companion object {

        fun fromNotificationsPlaceholders(
            type: CommunicationNotificationType,
            notificationPlaceholders: List<String>,
        ): CommunicationNotificationPlaceholderContent? {
            Log.d(TAG, "Parsing notification placeholders ($type): $notificationPlaceholders")

            return when (type) {
                StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_POST -> {
                    // ["courseTitle", "postTitle", "postContent", "postCreationDate", "postAuthorName", "imageUrl", "authorId", "postId"]
                    if (notificationPlaceholders.size <= 7) return null
                    val profilePic = notificationPlaceholders[5].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[4],
                        channelName = "New Announcement Post",
                        courseName = notificationPlaceholders[0],
                        userId = notificationPlaceholders[6],
                        messageId = notificationPlaceholders[7],
                        profilePicUrl = profilePic,
                        messageContent = "${notificationPlaceholders[1]}\n${notificationPlaceholders[2]}",
                        type = null,
                        isReply = false
                    )
                }

                is StandalonePostCommunicationNotificationType -> {
                    // ["courseTitle", "messageContent", "messageCreationDate", "conversationName", "authorName", "conversationType", "imageUrl", "userId", "postId"]
                    if (notificationPlaceholders.size <= 8) return null
                    val profilePic = notificationPlaceholders[6].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[4],
                        channelName = notificationPlaceholders[3],
                        courseName = notificationPlaceholders[0],
                        userId = notificationPlaceholders[7],
                        messageId = notificationPlaceholders[8],
                        profilePicUrl = profilePic,
                        messageContent = notificationPlaceholders[1],
                        type = ConversationType.fromString(notificationPlaceholders[5]),
                        isReply = false
                    )
                }
                               
                is ReplyPostCommunicationNotificationType -> {
                    // ["courseTitle", "postContent", "postCreationData", "postAuthorName", "answerPostContent", "answerPostCreationDate", "authorName", "conversationName", "imageUrl", "userId", "postingId", "parentPostId"]
                    if (notificationPlaceholders.size <= 11) return null
                    val profilePic = notificationPlaceholders[8].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = "Replied to ${notificationPlaceholders[6]} by ${notificationPlaceholders[3]}",
                        channelName = notificationPlaceholders[7],
                        courseName = notificationPlaceholders[0],
                        userId = notificationPlaceholders[9],
                        messageId = notificationPlaceholders[11],
                        profilePicUrl = profilePic,
                        messageContent = notificationPlaceholders[4],
                        type = null,
                        isReply = true
                    )
                }
            }
        }
    }
}