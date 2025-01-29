package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

enum class CommunicationNotificationConversationType(val rawValue: String) {
    CHANNEL("channel"),
    ONE_TO_ONE_CHAT("oneToOneChat"),
    GROUP_CHAT("groupChat");

    companion object {
        fun fromRawValue(rawValue: String): CommunicationNotificationConversationType =
            entries.first { it.rawValue == rawValue }
    }
}