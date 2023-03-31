package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.communicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.parentId
import kotlinx.datetime.Instant

@Dao
interface PushCommunicationDao {

    @Query("select exists(select * from push_communication where parent_id = :parentId and type = :type)")
    suspend fun hasPushCommunication(parentId: Long, type: CommunicationType): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPushCommunication(pushCommunicationEntity: PushCommunicationEntity)

    @Query("update push_communication set course_title = :courseTitle, title = :title where parent_id = :parentId and type = :type")
    suspend fun updatePushCommunication(
        parentId: Long,
        type: CommunicationType,
        courseTitle: String,
        title: String?
    )

    @Insert
    suspend fun insertCommunicationMessage(communicationMessageEntity: CommunicationMessageEntity)

    @Transaction
    suspend fun insertNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>,
        generateNotificationId: suspend () -> Int
    ) {
        val parentId = artemisNotification.parentId
        val type = artemisNotification.communicationType

        val courseTitle: String = artemisNotification.notificationPlaceholders[0]
        val postTitle: String = artemisNotification.notificationPlaceholders[1]
        val postContent: String = artemisNotification.notificationPlaceholders[2]
        // val postCreationDate: String = artemisNotification.notificationPlaceholders[3]
        val postAuthor: String = artemisNotification.notificationPlaceholders[4]

        val containerTitle: String? = when (artemisNotification.type) {
            StandalonePostCommunicationNotificationType.NEW_COURSE_POST,
            ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_COURSE_POST,
            StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_POST -> null

            StandalonePostCommunicationNotificationType.NEW_EXERCISE_POST, StandalonePostCommunicationNotificationType.NEW_LECTURE_POST ->
                artemisNotification.notificationPlaceholders[5]

            ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_EXERCISE_POST,
            ReplyPostCommunicationNotificationType.NEW_REPLY_FOR_LECTURE_POST -> artemisNotification.notificationPlaceholders[8]
        }

        if (hasPushCommunication(parentId, type)) {
            updatePushCommunication(parentId, type, courseTitle, postTitle)
        } else {
            val pushCommunicationEntity = PushCommunicationEntity(
                parentId = parentId,
                type = type,
                notificationId = generateNotificationId(),
                courseTitle = courseTitle,
                containerTitle = containerTitle,
                title = postTitle,
                target = artemisNotification.target
            )

            insertPushCommunication(pushCommunicationEntity)
        }

        val message: CommunicationMessageEntity = when (artemisNotification.type) {
            is StandalonePostCommunicationNotificationType -> CommunicationMessageEntity(
                communicationParentId = parentId,
                communicationType = type,
                title = postTitle,
                text = postContent,
                authorName = postAuthor,
                date = artemisNotification.date
            )

            is ReplyPostCommunicationNotificationType -> {
                val answerPostContent = artemisNotification.notificationPlaceholders[5]
                // val answerPostCreationDate = artemisNotification.notificationPlaceholders[6]
                val answerPostAuthor = artemisNotification.notificationPlaceholders[7]

                CommunicationMessageEntity(
                    communicationParentId = parentId,
                    communicationType = type,
                    title = null,
                    text = answerPostContent,
                    authorName = answerPostAuthor,
                    date = artemisNotification.date
                )
            }
        }

        insertCommunicationMessage(message)
    }

    @Transaction
    suspend fun insertSelfMessage(
        parentId: Long,
        type: CommunicationType,
        authorName: String,
        body: String,
        date: Instant
    ) {
        if (hasPushCommunication(parentId, type)) {
            insertCommunicationMessage(
                CommunicationMessageEntity(
                    communicationParentId = parentId,
                    communicationType = type,
                    title = null,
                    text = body,
                    authorName = authorName,
                    date = date
                )
            )
        }
    }

    @Query("select * from push_communication where parent_id = :parentId and type = :type")
    suspend fun getCommunication(parentId: Long, type: CommunicationType): PushCommunicationEntity

    @Query("select * from push_communication_message where communication_parent_id = :parentId and communication_type = :type order by id asc")
    suspend fun getCommunicationMessages(
        parentId: Long,
        type: CommunicationType
    ): List<CommunicationMessageEntity>

    @Query("delete from push_communication where parent_id = :parentId and type = :type")
    suspend fun deleteCommunication(parentId: Long, type: CommunicationType)
}