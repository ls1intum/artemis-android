package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil.request.ImageRequest

interface ProfilePictureImageProvider {
    fun createImageRequest(
        context: Context,
        imagePath: String,
    ): ImageRequest
}

class ProfilePictureImageProviderImpl(
    private val serverUrl: String,
    private val authToken: String
) : ProfilePictureImageProvider {

    private val imageProvider = DefaultImageProvider()

    override fun createImageRequest(
        context: Context,
        imagePath: String,
    ): ImageRequest {
        return imageProvider.createImageRequest(
            context,
            imagePath,
            serverUrl,
            authToken,
            memoryCacheKey = serverUrl + imagePath
        )
    }
}