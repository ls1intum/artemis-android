package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest

interface BaseImageProvider {
    fun createImageRequest(
        context: Context,
        imagePath: String,
        serverUrl: String,
        authorizationToken: String
    ): ImageRequest

    fun createImageLoader(context: Context, authorizationToken: String): ImageLoader
}
