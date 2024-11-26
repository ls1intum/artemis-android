package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import coil.compose.AsyncImagePainter


val LocalArtemisImageProvider = compositionLocalOf<ArtemisImageProvider> { error("No ArtemisImageProvider provided") }

/**
 * Provides a way to load images from the Artemis server. This interface implementation takes care
 * of authentication and the applicable Artemis server URL.
 */
interface ArtemisImageProvider {

    @Composable
    fun rememberArtemisAsyncImagePainter(
        imagePath: String,
    ): AsyncImagePainter
}

