package de.tum.informatics.www1.artemis.native_app.feature.push

import com.google.firebase.messaging.FirebaseMessagingService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import org.koin.android.ext.android.get

class ArtemisFirebaseMessagingService : FirebaseMessagingService() {

    private val serverConfigurationService: ServerConfigurationService = get()

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Sent this new token to the server if push is enabled

    }
}