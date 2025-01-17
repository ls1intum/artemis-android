package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil3.request.ImageRequest

interface BaseImageProvider {
    fun createImageRequest(
        context: Context,
        imageUrl: String,
        authorizationToken: String?,
        memoryCacheKey: String? = null
    ): ImageRequest

    // The markwon library still uses Coil 2
    fun createCoil2ImageLoader(context: Context, authorizationToken: String): coil.ImageLoader
}
