package de.tum.informatics.www1.artemis.native_app.feature.metistest

import android.content.Context
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ProfilePictureImageProvider

class ProfilePictureImageProviderStub: ProfilePictureImageProvider {

    private val sampleImageUrl = "https://picsum.photos/200"

    override fun createImageRequest(context: Context, imagePath: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(sampleImageUrl)
            .build()
    }

}