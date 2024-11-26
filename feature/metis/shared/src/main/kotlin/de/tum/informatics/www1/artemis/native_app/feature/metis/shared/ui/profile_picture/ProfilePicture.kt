package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.loadAsyncImageDrawable
import de.tum.informatics.www1.artemis.native_app.core.ui.common.nonScaledSp
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ProfilePictureImageProvider

const val TEST_TAG_PROFILE_PICTURE_IMAGE = "TEST_TAG_PROFILE_PICTURE_IMAGE"
const val TEST_TAG_PROFILE_PICTURE_INITIALS = "TEST_TAG_PROFILE_PICTURE_INITIALS"

private const val BoxSizeToFontSizeMultiplier = 0.16f


@Composable
fun ProfilePicture(
    modifier: Modifier,
    profilePictureData: ProfilePictureData,
    profilePictureImageProvider: ProfilePictureImageProvider?,
) {
    // TODO: Add onClick that opens a dialog with info about the user, see iOS
    //       https://github.com/ls1intum/artemis-android/issues/154
    when(profilePictureData) {
        is ProfilePictureData.Image -> {
            if (profilePictureImageProvider != null) {
                ProfilePictureImage(
                    modifier = modifier,
                    profilePictureData = profilePictureData,
                    profilePictureImageProvider = profilePictureImageProvider,
                )
            } else {
                InitialsPlaceholder(
                    modifier = modifier,
                    profilePictureData = profilePictureData.fallBack,
                )
            }
        }
        is ProfilePictureData.InitialsPlaceholder -> {
            InitialsPlaceholder(
                modifier = modifier,
                profilePictureData = profilePictureData,
            )
        }
    }
}

@Composable
fun ProfilePictureImage(
    modifier: Modifier,
    profilePictureData: ProfilePictureData.Image,
    profilePictureImageProvider: ProfilePictureImageProvider,
) {
    val imageUrl = profilePictureData.url
    val context = LocalContext.current
    val request = remember(imageUrl, profilePictureImageProvider) {
        profilePictureImageProvider.createImageRequest(
            context = context,
            imagePath = imageUrl,
        )
    }

    when (val resultDataState = loadAsyncImageDrawable(request = request).dataState) {
        is DataState.Failure -> {
            InitialsPlaceholder(
                modifier = modifier,
                profilePictureData = profilePictureData.fallBack,
            )
        }
        is DataState.Loading -> {
            InitialsPlaceholder(
                modifier = modifier,
                profilePictureData = profilePictureData.fallBack,
            )
        }
        is DataState.Success -> {
            val loadedDrawable = resultDataState.data

            val painter = remember(loadedDrawable) {
                val bitmap = loadedDrawable.toBitmap().asImageBitmap()
                BitmapPainter(bitmap)
            }

            Image(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag(TEST_TAG_PROFILE_PICTURE_IMAGE),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun InitialsPlaceholder(
    modifier: Modifier,
    profilePictureData: ProfilePictureData.InitialsPlaceholder,
) {
    val boxSize = remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .background(color = profilePictureData.backgroundColor)
            .onGloballyPositioned {
                boxSize.intValue = it.size.width
            }
            .testTag(TEST_TAG_PROFILE_PICTURE_INITIALS),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = profilePictureData.initials,
            color = Color.White,
            fontSize = boxSize.intValue.sp.nonScaledSp * BoxSizeToFontSizeMultiplier,
            fontWeight =  FontWeight.Bold
        )
    }
}




