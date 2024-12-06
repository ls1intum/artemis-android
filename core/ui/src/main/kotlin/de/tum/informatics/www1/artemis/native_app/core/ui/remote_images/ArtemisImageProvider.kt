package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import coil.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest


val LocalArtemisImageProvider = compositionLocalOf<ArtemisImageProvider> { error("No ArtemisImageProvider provided") }

/**
 * Provides a way to load images from the Artemis server. This interface implementation takes care
 * of authentication and the applicable Artemis server URL.
 */
interface ArtemisImageProvider {

    @Composable
    fun rememberArtemisImageRequest(
        imagePath: String,
    ): ImageRequest

    @Composable
    fun rememberArtemisAsyncImagePainter(
        imagePath: String,
    ): AsyncImagePainter = rememberAsyncImagePainter(model = rememberArtemisImageRequest(imagePath))

    @Composable
    fun rememberArtemisImageLoader() : ImageLoader
}

