package de.tum.informatics.www1.artemis.native_app.feature.push

import android.app.NotificationManager
import androidx.annotation.StringRes

enum class ArtemisNotificationChannel(
    val id: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    MiscNotificationChannel(
        "misc-notification-channel",
        R.string.push_notification_channel_misc_name,
        R.string.push_notification_channel_misc_description
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
        R.string.push_notification_channel_course_post_description
    ),
    ExercisePostNotificationChannel(
        "exercise-post-notification-channel",
        R.string.push_notification_channel_exercise_post_name,
        R.string.push_notification_channel_exercise_post_description
    ),
    LecturePostNotificationChannel(
        "lecture-post-notification-channel",
        R.string.push_notification_channel_lecture_post_name,
        R.string.push_notification_channel_lecture_post_description
    )
}