package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.annotation.DrawableRes
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.R

enum class CommunicationType(val notificationChannel: ArtemisNotificationChannel, @DrawableRes val notificationIcon: Int) {
    ANNOUNCEMENT(ArtemisNotificationChannel.CourseAnnouncementNotificationChannel, R.drawable.baseline_campaign_24),
    QNA_COURSE(ArtemisNotificationChannel.CoursePostNotificationChannel, R.drawable.baseline_contact_support_24),
    QNA_EXERCISE(ArtemisNotificationChannel.ExercisePostNotificationChannel, R.drawable.baseline_contact_support_24),
    QNA_LECTURE(ArtemisNotificationChannel.LecturePostNotificationChannel, R.drawable.baseline_contact_support_24),
    CONVERSATION(ArtemisNotificationChannel.CommunicationNotificationChannel, R.drawable.baseline_chat_bubble_24)
}