package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult


val LocalArtemisImageProvider = compositionLocalOf<ArtemisImageProvider> { EmptyArtemisImageProvider() }

/**
 * Provides a way to load images from the Artemis server. This interface implementation takes care
 * of authentication and the applicable Artemis server URL.
 */
interface ArtemisImageProvider {

    suspend fun loadArtemisImage(
        context: Context,
        imagePath: String,
    ): ImageResult

    @Composable
    fun rememberArtemisImageRequest(
        imagePath: String,
    ): ImageRequest

    @Composable
    fun rememberArtemisAsyncImagePainter(
        imagePath: String,
    ): AsyncImagePainter = rememberAsyncImagePainter(model = rememberArtemisImageRequest(imagePath))

    @Composable
    fun rememberArtemisImageRequestByUrl(imageUrl: String): ImageRequest

    @Composable
    fun rememberArtemisAsyncImagePainterByUrl(imageUrl: String): AsyncImagePainter =
        rememberAsyncImagePainter(model = rememberArtemisImageRequestByUrl(imageUrl))

    @Composable
    fun rememberArtemisImageLoader() : ImageLoader
}

private class EmptyArtemisImageProvider : ArtemisImageProvider {

    override suspend fun loadArtemisImage(context: Context, imagePath: String): ImageResult {
        return ErrorResult(
            image = null,
            request = ImageRequest.Builder(context).data(imagePath).build(),
            throwable = IllegalStateException("No ArtemisImageProvider provided."),
        )
    }

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current).build()
    }

    @Composable
    override fun rememberArtemisImageLoader(): ImageLoader {
        return ImageLoader(LocalContext.current)
    }

    @Composable
    override fun rememberArtemisImageRequestByUrl(imageUrl: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current).build()
    }
}

