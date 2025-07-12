package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget


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
    val courseName: String,
    val channelName: String,
    val authorId: String,
    val authorName: String,
    val authorImageUrl: String?,
    val messageId: String,
    val messageContent: String,
    val conversationType: CommunicationNotificationConversationType?,
    val isReply: Boolean
) {
    companion object {

        fun fromNotificationsPlaceholders(
            type: CommunicationNotificationType,
            notificationPlaceholders: List<String>,
            target: CommunicationPostTarget? = null,
        ): CommunicationNotificationPlaceholderContent? {
            Log.d(TAG, "Parsing notification placeholders ($type): $notificationPlaceholders")

            return when (type) {
                StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_NOTIFICATION -> {
                    // ["courseTitle", "postTitle", "postContent", "postCreationDate", "postAuthorName", "imageUrl", "authorId", "postId"]
                    if (notificationPlaceholders.size <= 7) return null
                    val profilePic = notificationPlaceholders[5].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[4],
                        channelName = "New Announcement Post",
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[6],
                        messageId = notificationPlaceholders[7],
                        authorImageUrl = profilePic,
                        messageContent = "${notificationPlaceholders[1]}\n${notificationPlaceholders[2]}",
                        conversationType = null,
                        isReply = false
                    )
                }

                // Regular conversation message
                StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION -> {
                    // ["courseTitle", "messageContent", "messageCreationDate", "conversationName", "authorName", "conversationType", "imageUrl", "userId", "postId"]
                    if (notificationPlaceholders.size <= 8) return null
                    val profilePic = notificationPlaceholders[6].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[4],
                        channelName = notificationPlaceholders[3],
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[7],
                        messageId = notificationPlaceholders[8],
                        authorImageUrl = profilePic,
                        messageContent = notificationPlaceholders[1],
                        conversationType = CommunicationNotificationConversationType.fromRawValue(notificationPlaceholders[5]),
                        isReply = false
                    )
                }
                               
                is ReplyPostCommunicationNotificationType -> {
                    // ["courseTitle", "postContent", "postCreationData", "postAuthorName", "answerPostContent", "answerPostCreationDate", "authorName", "conversationName", "imageUrl", "userId", "postingId", "parentPostId"]
                    if (notificationPlaceholders.size <= 11) return null
                    val profilePic = notificationPlaceholders[8].takeIf { it.isNotEmpty() }
                    var content = notificationPlaceholders[4]

                    // There is a bug for notifications for user mentions in conversations. The placeholders
                    // are filled like they would for a new reply, even when the notification is for a user mention
                    // in a base post. We treat this case here.
                    if (type == ReplyPostCommunicationNotificationType.NEW_MENTION_NOTIFICATION &&
                        target?.message == CommunicationPostTarget.MESSAGE_NEW_MESSAGE) {
                        content = notificationPlaceholders[1]
                    }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[6],
                        channelName = notificationPlaceholders[7],
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[9],
                        messageId = notificationPlaceholders[11],
                        authorImageUrl = profilePic,
                        messageContent = content,
                        conversationType = null,
                        isReply = true
                    )
                }

                // Channel management notification
                StandalonePostCommunicationNotificationType.ADDED_TO_CHANNEL_NOTIFICATION -> {
                    // ["courseTitle", "channelName", "channelModerator", "authorImageUrl", "authorId"]
                    if (notificationPlaceholders.size < 5) return null
                    val profilePic = notificationPlaceholders[3].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[2],
                        channelName = notificationPlaceholders[1],
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[4],
                        messageId = "0",
                        authorImageUrl = profilePic,
                        messageContent = "You have been added to the channel",
                        conversationType = null,
                        isReply = false
                    )
                }

                StandalonePostCommunicationNotificationType.REMOVED_FROM_CHANNEL_NOTIFICATION -> {
                    // ["courseTitle", "channelName", "channelModerator", "authorImageUrl", "authorId"]
                    if (notificationPlaceholders.size < 5) return null
                    val profilePic = notificationPlaceholders[3].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[2],
                        channelName = notificationPlaceholders[1],
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[4],
                        messageId = "0",
                        authorImageUrl = profilePic,
                        messageContent = "You have been removed from the channel",
                        conversationType = null,
                        isReply = false
                    )
                }

                StandalonePostCommunicationNotificationType.CHANNEL_DELETED_NOTIFICATION -> {
                    // ["courseTitle", "channelName", "deletingUser", "authorImageUrl", "authorId"]
                    if (notificationPlaceholders.size < 5) return null
                    val profilePic = notificationPlaceholders[3].takeIf { it.isNotEmpty() }

                    CommunicationNotificationPlaceholderContent(
                        authorName = notificationPlaceholders[2],
                        channelName = notificationPlaceholders[1],
                        courseName = notificationPlaceholders[0],
                        authorId = notificationPlaceholders[4],
                        messageId = "0",
                        authorImageUrl = profilePic,
                        messageContent = "The channel has been deleted",
                        conversationType = null,
                        isReply = false
                    )
                }
            }
        }
    }
}