package de.tum.informatics.www1.artemis.native_app.core.ui.test

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.test.FakeImage
import coil3.test.FakeImageLoaderEngine
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider


class ArtemisImageProviderStub : ArtemisImageProvider {

    fun setup() {
        val engine = FakeImageLoaderEngine.Builder()
            .default(FakeImage(color = 0x00F))
            .build()
        val imageLoader = ImageLoader.Builder(context)
            .components { add(engine) }
            .build()
        SingletonImageLoader.setUnsafe(imageLoader)
    }

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .build()
    }
}