package de.tum.informatics.www1.artemis.native_app.feature.push.service

interface PushNotificationCipher {

    fun decipherPushNotification(ciphertext: String, iv: String): String?
}
