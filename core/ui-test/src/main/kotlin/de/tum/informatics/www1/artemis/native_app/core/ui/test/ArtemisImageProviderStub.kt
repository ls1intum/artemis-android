package de.tum.informatics.www1.artemis.native_app.core.ui.test

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.request.ImageRequest
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

    @Composable
    override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .build()
    }
}