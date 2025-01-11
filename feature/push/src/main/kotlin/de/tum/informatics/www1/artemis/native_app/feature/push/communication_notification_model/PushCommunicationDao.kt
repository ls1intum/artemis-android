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
        generateNotificationId: suspend () -> Int
    ) {
        val parentId = artemisNotification.parentId

        val message: CommunicationMessageEntity = try {
            val placeholderContent = CommunicationNotificationPlaceholderContent
                .fromArtemisNotification(artemisNotification)

            if (hasPushCommunication(parentId)) {
                updatePushCommunication(
                    parentId = parentId,
                    courseTitle = placeholderContent.courseTitle
                )
            } else {
                val pushCommunicationEntity = PushCommunicationEntity(
                    parentId = parentId,
                    notificationId = generateNotificationId(),
                    courseTitle = placeholderContent.courseTitle,
                    containerTitle = placeholderContent.containerTitle,
                    target = artemisNotification.target
                )

                insertPushCommunication(pushCommunicationEntity)
            }

            placeholderContent.toCommunicationMessageEntity(
                parentId = parentId,
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
        authorName: String,
        body: String,
        date: Instant
    ) {
        if (hasPushCommunication(parentId)) {
            insertCommunicationMessage(
                CommunicationMessageEntity(
                    communicationParentId = parentId,
                    text = body,
                    authorName = authorName,
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