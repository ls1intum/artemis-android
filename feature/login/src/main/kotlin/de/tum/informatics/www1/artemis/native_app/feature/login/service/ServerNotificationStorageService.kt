package de.tum.informatics.www1.artemis.native_app.feature.login.service

/**
 * Allows to set and retrieve if the notification dialog has already been displayed after a login
 * for the given server
 */
interface ServerNotificationStorageService {

    /**
     * @return if the notification settings have already been displayed
     */
    suspend fun hasDisplayedForServer(serverUrl: String): Boolean

    /**
     * Set that the notification settings have been displayed for the provided server
     */
    suspend fun setHasDisplayed(serverUrl: String)
}