package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.BaseImageProvider
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.first

class ArtemisImageProviderImpl(
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val imageProvider: BaseImageProvider
) : ArtemisImageProvider {

    override suspend fun loadArtemisImage(context: Context, imagePath: String): ImageResult {
        val serverUrl = serverConfigurationService.serverUrl.first()
        val authToken = accountService.authToken.first()

        val imageUrl = URLBuilder(serverUrl).appendPathSegments(imagePath).buildString()
        val request = imageProvider.createImageRequest(
            context = context,
            imageUrl = imageUrl,
            authorizationToken = authToken,
        )

        val loader = coil3.ImageLoader(context)
        return loader.execute(request)
    }

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
            imageProvider.createCoil2ImageLoader(context, authorizationToken)
        }
    }
}