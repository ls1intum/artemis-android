package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.test.FakeImage
import coil3.test.FakeImageLoaderEngine
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider


@OptIn(DelicateCoilApi::class)
class ArtemisImageProviderStub : ArtemisImageProvider {

    companion object {
        fun setup(context: Context) {
            // For more info on testing coil, see: https://coil-kt.github.io/coil/testing/
            val engine = FakeImageLoaderEngine.Builder()
                .default(FakeImage(color = 0x00F))
                .build()
            val imageLoader = ImageLoader.Builder(context)
                .components { add(engine) }
                .build()
            SingletonImageLoader.setUnsafe(imageLoader)
        }
    }

    override suspend fun loadArtemisImage(context: Context, imagePath: String): ImageResult {
        return ErrorResult(
            image = null,
            request = ImageRequest.Builder(context).data(imagePath).build(),
            throwable = Exception("Fake error"),
        )
    }

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .build()
    }

    @Composable
    override fun rememberArtemisImageLoader(): coil.ImageLoader {
        return coil.ImageLoader(LocalContext.current)
    }
}