package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.loadAsyncImageDrawable
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ProfilePictureImageProvider

@Composable
fun ProfilePicture(
    modifier: Modifier,
    profilePictureData: ProfilePictureData,
    profilePictureImageProvider: ProfilePictureImageProvider,
) {
    // TODO
    if (profilePictureData is ProfilePictureData.InitialsPlaceholder) return

    ProfilePictureImage(
        modifier = modifier,
        imageUrl = (profilePictureData as ProfilePictureData.Image).url,
        profilePictureImageProvider = profilePictureImageProvider,
    )
}

@Composable
fun ProfilePictureImage(
    modifier: Modifier,
    imageUrl: String,
    profilePictureImageProvider: ProfilePictureImageProvider,
) {
    val context = LocalContext.current

    val request = remember(imageUrl, profilePictureImageProvider) {
        profilePictureImageProvider.createImageRequest(
            context = context,
            imagePath = imageUrl,
        )
    }

    val resultData = loadAsyncImageDrawable(request = request)

    // TODO: draw image if resultData dataState is Success
}


