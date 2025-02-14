package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete

import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData

data class AutoCompleteHint(
    val hint: String,
    val replacementText: String,
    val id: String,
    val icon: AutoCompleteIcon? = null
)

sealed class AutoCompleteIcon {
    data class ProfilePicture(val pictureData: ProfilePictureData) : AutoCompleteIcon()
    data class DrawableFromId(val id: Int?) : AutoCompleteIcon()
    data class DrawableFromImageVector(val imageVector: ImageVector) : AutoCompleteIcon()
}

