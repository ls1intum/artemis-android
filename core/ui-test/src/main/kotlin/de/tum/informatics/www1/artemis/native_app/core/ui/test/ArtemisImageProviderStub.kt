package de.tum.informatics.www1.artemis.native_app.core.ui.test

import android.graphics.Insets.add


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