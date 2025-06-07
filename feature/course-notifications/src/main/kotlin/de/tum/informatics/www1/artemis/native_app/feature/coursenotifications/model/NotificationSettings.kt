package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model


import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.R
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotificationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingsInfo(
    val notificationTypes: Map<String, CourseNotificationType>,
    val channels: List<NotificationChannel>,
    val presets: List<NotificationSettingsPreset>
)

@Serializable
data class NotificationSettings(
    var selectedPreset: Int,
    var notificationTypeChannels: Map<String, Map<NotificationChannel, Boolean>>
)

@Serializable
data class NotificationSettingsPreset(
    val identifier: NotificationSettingsPresetIdentifier,
    val typeId: Int,
    val presetMap: Map<CourseNotificationType, Map<NotificationChannel, Boolean>>
)

@Serializable
enum class NotificationSettingsPresetIdentifier(val presetId: Int, @StringRes val titleResId: Int, @StringRes val descriptionResId: Int) {
    @SerialName("defaultUserCourseNotificationSettingPreset")
    DEFAULT_USER_COURSE_NOTIFICATION_SETTING_PRESET(
        1,
        R.string.settings_preset_default,
        R.string.settings_preset_description_default
    ),
    @SerialName("allActivityUserCourseNotificationSettingPreset")
    ALL_ACTIVITY_USER_COURSE_NOTIFICATION_SETTING_PRESET(
        2,
        R.string.settings_preset_all_activity,
        R.string.settings_preset_description_all_activity
    ),
    @SerialName("ignoreUserCourseNotificationSettingPreset")
    IGNORE_USER_COURSE_NOTIFICATION_SETTING_PRESET(
        3,
        R.string.settings_preset_ignore_all,
        R.string.settings_preset_description_ignore_all
    ),
    @SerialName("custom")
    CUSTOM(
        0,
        R.string.settings_preset_custom,
        R.string.settings_preset_description_custom
    ),
    @SerialName("unknown")
    UNKNOWN(-1, 0, 0);
}