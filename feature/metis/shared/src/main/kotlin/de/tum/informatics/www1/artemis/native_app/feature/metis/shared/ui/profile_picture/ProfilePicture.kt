package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import de.tum.informatics.www1.artemis.native_app.core.ui.common.nonScaledSp
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

const val TEST_TAG_PROFILE_PICTURE_IMAGE = "TEST_TAG_PROFILE_PICTURE_IMAGE"
const val TEST_TAG_PROFILE_PICTURE_INITIALS = "TEST_TAG_PROFILE_PICTURE_INITIALS"
const val TEST_TAG_PROFILE_PICTURE_UNKNOWN = "TEST_TAG_PROFILE_PICTURE_UNKNOWN"

private const val BoxSizeToFontSizeMultiplier = 0.16f


@Composable
fun ProfilePictureWithDialog(
    modifier: Modifier = Modifier,
    conversationUser: ConversationUser,
) = ProfilePictureWithDialog(
    modifier = modifier,
    userId = conversationUser.id,
    userName = conversationUser.humanReadableName,
    userRole = conversationUser.getUserRole(),
    imageUrl = conversationUser.imageUrl
)


@Composable
fun ProfilePictureWithDialog(
    modifier: Modifier = Modifier,
    userId: Long,
    userName: String,
    userRole: UserRole?,
    imageUrl: String?,
) {
    var displayUserProfileDialog by remember{ mutableStateOf(false) }

    val profilePictureData = ProfilePictureData.create(
        userId = userId,
        username = userName,
        imageUrl = imageUrl,
    )

    ProfilePicture(
        modifier = modifier.clickable {
            displayUserProfileDialog = true
        },
        profilePictureData = profilePictureData,
    )

    if (displayUserProfileDialog) {
        UserProfileDialog(
            username = userName,
            userRole = userRole,
            profilePictureData = profilePictureData,
            onDismiss = {
                displayUserProfileDialog = false
            },
        )
    }
}

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    profilePictureData: ProfilePictureData,
) {
    val modifierWithDefault = modifier
        .size(30.dp)
        .clip(shape = RoundedCornerShape(percent = 15))

    when(profilePictureData) {
        is ProfilePictureData.Image -> {
            ProfilePictureImage(
                modifier = modifierWithDefault,
                profilePictureData = profilePictureData,
            )
        }
        is ProfilePictureData.InitialsPlaceholder -> {
            InitialsPlaceholder(
                modifier = modifierWithDefault.testTag(TEST_TAG_PROFILE_PICTURE_INITIALS),
                profilePictureData = profilePictureData,
            )
        }
        ProfilePictureData.Unknown -> {
            InitialsPlaceholder(
                modifier = modifierWithDefault.testTag(TEST_TAG_PROFILE_PICTURE_UNKNOWN),
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
    val painterState by painter.state.collectAsState()

    if (painterState is AsyncImagePainter.State.Error) {
        Log.e("ProfilePicture", "Error loading image: ${(painterState as AsyncImagePainter.State.Error).result.throwable.message}")
    }

    when (painterState) {
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




