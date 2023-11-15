package de.tum.informatics.www1.artemis.native_app.feature.push.service

interface PushNotificationHandler {

    /**
     * Handles the receiving of a push notification. The push notification has to be decrypted.
     * Based on the type stores it and shows it to the user.
     */
    fun handleServerPushNotification(payload: String)
}
