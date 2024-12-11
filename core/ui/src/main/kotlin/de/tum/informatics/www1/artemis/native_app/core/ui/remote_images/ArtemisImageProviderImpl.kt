package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken

class ArtemisImageProviderImpl(
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
) : ArtemisImageProvider {

    private val imageProvider = BaseImageProviderImpl()

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")
        val authToken by accountService.authToken.collectAsState(initial = "")

        val context = LocalContext.current
        return remember(serverUrl, authToken, imagePath) {
            imageProvider.createImageRequest(
                context = context,
                imagePath = imagePath,
                serverUrl = serverUrl,
                authorizationToken = authToken,
                memoryCacheKey = serverUrl + imagePath
            )
        }
    }

}