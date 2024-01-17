package de.tum.informatics.www1.artemis.native_app.core.common

import android.app.NotificationManager

enum class ArtemisNotificationChannel(
    val id: String,
    val title: Int,
    val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    MiscNotificationChannel(
        "misc-notification-channel",
        R.string.push_notification_channel_misc_name,
        R.string.push_notification_channel_misc_description,
        NotificationManager.IMPORTANCE_DEFAULT
    ),
    CourseAnnouncementNotificationChannel(
        "course-announcement-notification-channel",
        R.string.push_notification_channel_course_announcement_name,
        R.string.push_notification_channel_course_announcement_description,
        importance = NotificationManager.IMPORTANCE_HIGH
    ),
    CoursePostNotificationChannel(
        "course-post-notification-channel",
        R.string.push_notification_channel_course_post_name,
        R.string.push_notification_channel_course_post_description,
        NotificationManager.IMPORTANCE_HIGH
    ),
    ExercisePostNotificationChannel(
        "exercise-post-notification-channel",
        R.string.push_notification_channel_exercise_post_name,
        R.string.push_notification_channel_exercise_post_description,
        NotificationManager.IMPORTANCE_HIGH
    ),
    LecturePostNotificationChannel(
        "lecture-post-notification-channel",
        R.string.push_notification_channel_lecture_post_name,
        R.string.push_notification_channel_lecture_post_description,
        NotificationManager.IMPORTANCE_HIGH
    ),
    CommunicationNotificationChannel(
        "communication-notification-channel",
        R.string.push_notification_channel_communication_name,
        R.string.push_notification_channel_communication_description,
        NotificationManager.IMPORTANCE_HIGH
    )
}