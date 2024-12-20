package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil.ImageLoader
import coil3.request.ImageRequest

interface BaseImageProvider {
    fun createImageRequest(
        context: Context,
        imageUrl: String,
        authorizationToken: String,
        memoryCacheKey: String? = null
    ): ImageRequest

    fun createImageLoader(context: Context, authorizationToken: String): ImageLoader
}
