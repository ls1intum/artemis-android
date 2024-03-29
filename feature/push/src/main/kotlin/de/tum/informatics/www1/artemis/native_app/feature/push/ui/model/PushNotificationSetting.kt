package de.tum.informatics.www1.artemis.native_app.feature.push.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class PushNotificationSetting(
    val id: Long = 0L,
    val settingId: String = "",
    val webapp: Boolean? = null,
    val email: Boolean? = null,
    val push: Boolean? = null
)

val PushNotificationSetting.group: String
    get() {
        return settingId.split('.').getOrNull(1).orEmpty()
    }

val PushNotificationSetting.setting: String
    get() = settingId.split('.').getOrNull(2).orEmpty()