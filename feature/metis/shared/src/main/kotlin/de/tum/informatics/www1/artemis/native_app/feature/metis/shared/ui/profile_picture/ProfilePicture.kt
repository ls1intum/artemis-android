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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import de.tum.informatics.www1.artemis.native_app.core.ui.common.nonScaledSp
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider

const val TEST_TAG_PROFILE_PICTURE_IMAGE = "TEST_TAG_PROFILE_PICTURE_IMAGE"
const val TEST_TAG_PROFILE_PICTURE_INITIALS = "TEST_TAG_PROFILE_PICTURE_INITIALS"
const val TEST_TAG_PROFILE_PICTURE_UNKNOWN = "TEST_TAG_PROFILE_PICTURE_UNKNOWN"

private const val BoxSizeToFontSizeMultiplier = 0.16f


@Composable
fun ProfilePicture(
    modifier: Modifier,
    profilePictureData: ProfilePictureData,
) {
    // TODO: Add onClick that opens a dialog with info about the user, see iOS
    //       https://github.com/ls1intum/artemis-android/issues/154

    when(profilePictureData) {
        is ProfilePictureData.Image -> {
            ProfilePictureImage(
                modifier = modifier,
                profilePictureData = profilePictureData,
            )
        }
        is ProfilePictureData.InitialsPlaceholder -> {
            InitialsPlaceholder(
                modifier = modifier.testTag(TEST_TAG_PROFILE_PICTURE_INITIALS),
                profilePictureData = profilePictureData,
            )
        }
        ProfilePictureData.Unknown -> {
            InitialsPlaceholder(
                modifier = modifier.testTag(TEST_TAG_PROFILE_PICTURE_UNKNOWN),
                profilePictureData = ProfilePictureData.InitialsPlaceholder(0, "?"),
            )
        }
    }
}

@Composable
fun ProfilePictureImage(
    modifier: Modifier,
    profilePictureData: ProfilePictureData.Image,
) {
    val imageUrl = profilePictureData.url
    val artemisImageProvider = LocalArtemisImageProvider.current
    val painter = artemisImageProvider.rememberArtemisAsyncImagePainter(imagePath = imageUrl)

    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            Image(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag(TEST_TAG_PROFILE_PICTURE_IMAGE),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
        else -> {
            InitialsPlaceholder(
                modifier = modifier,
                profilePictureData = profilePictureData.fallBack,
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
            },
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




