package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.parentId
import kotlinx.datetime.Instant

@Dao
interface PushCommunicationDao {

    companion object {
        private const val TAG = "PushCommunicationDao"
    }

    @Query("select exists(select * from push_communication where parent_id = :parentId)")
    suspend fun hasPushCommunication(parentId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPushCommunication(pushCommunicationEntity: PushCommunicationEntity)

    @Query("update push_communication set course_title = :courseTitle where parent_id = :parentId")
    suspend fun updatePushCommunication(
        parentId: Long,
        courseTitle: String,
    )

    @Insert
    suspend fun insertCommunicationMessage(communicationMessageEntity: CommunicationMessageEntity)

    @Transaction
    suspend fun insertNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>,
        generateNotificationId: suspend () -> Int,
        isPostFromAppUser: suspend (CommunicationNotificationPlaceholderContent) -> Boolean
    ) {
        val parentId = artemisNotification.parentId

        val message: CommunicationMessageEntity = try {
            val content = CommunicationNotificationPlaceholderContent.fromNotificationsPlaceholders(
                type = artemisNotification.type,
                notificationPlaceholders = artemisNotification.notificationPlaceholders
            ) ?: return

            if (isPostFromAppUser(content)) {
                // As a temporary fix for receiving notifications for own posts.
                // See: https://github.com/ls1intum/artemis-android/issues/326
                return
            }

            if (hasPushCommunication(parentId)) {
                updatePushCommunication(
                    parentId = parentId,
                    courseTitle = content.courseName
                )
            } else {
                val pushCommunicationEntity = PushCommunicationEntity(
                    parentId = parentId,
                    notificationId = generateNotificationId(),
                    notificationTypeString = artemisNotification.type.toString(),
                    courseTitle = content.courseName,
                    containerTitle = content.channelName,
                    targetString = artemisNotification.target,
                    conversationTypeString = content.conversationType?.rawValue
                )

                insertPushCommunication(pushCommunicationEntity)
            }

            CommunicationMessageEntity(
                communicationParentId = parentId,
                text = content.messageContent,
                authorId = content.authorId,
                authorName = content.authorName,
                authorImageUrl = content.authorImageUrl,
                date = artemisNotification.date
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error while parsing artemis communication notification", e)
            return
        }

        insertCommunicationMessage(message)
    }

    @Transaction
    suspend fun insertSelfMessage(
        parentId: Long,
        authorLoginName: String,
        authorName: String,
        authorImageUrl: String?,
        body: String,
        date: Instant
    ) {
        if (hasPushCommunication(parentId)) {
            insertCommunicationMessage(
                CommunicationMessageEntity(
                    communicationParentId = parentId,
                    text = body,
                    authorId = authorLoginName,
                    authorName = authorName,
                    authorImageUrl = authorImageUrl,
                    date = date
                )
            )
        }
    }

    @Query("select * from push_communication where parent_id = :parentId")
    suspend fun getCommunication(parentId: Long): PushCommunicationEntity

    @Query("select * from push_communication_message where communication_parent_id = :parentId order by id asc")
    suspend fun getCommunicationMessages(
        parentId: Long
    ): List<CommunicationMessageEntity>

    @Query("delete from push_communication where parent_id = :parentId")
    suspend fun deleteCommunication(parentId: Long)
}