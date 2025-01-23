package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil3.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

class ArtemisImageProviderImpl(
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
) : ArtemisImageProvider {

    private val imageProvider = BaseImageProviderImpl()

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")
        val authToken by accountService.authToken.collectAsState(initial = "")

        val imageUrl = URLBuilder(serverUrl).appendPathSegments(imagePath).buildString()
        val context = LocalContext.current

        return remember(imageUrl, authToken) {
            imageProvider.createImageRequest(
                context = context,
                imageUrl = imageUrl,
                authorizationToken = authToken,
            )
        }
    }

    @Composable
    override fun rememberArtemisImageLoader(): ImageLoader {
        val authorizationToken by accountService.authToken.collectAsState(initial = "")
        val context = LocalContext.current

        return remember(authorizationToken) {
            imageProvider.createImageLoader(context, authorizationToken)
        }
    }
}