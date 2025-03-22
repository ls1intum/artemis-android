package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.settings.R

internal sealed interface ProfilePictureUploadResult {

    data object Success : ProfilePictureUploadResult

    sealed interface Error : ProfilePictureUploadResult
    data object ImageCouldNotBeCompressed : Error
    data object UploadFailed : Error
}


internal fun ProfilePictureUploadResult.Error.getMessage(context: Context): String = when (this) {
    ProfilePictureUploadResult.ImageCouldNotBeCompressed -> context.getString(R.string.profile_picture_upload_error_could_not_be_compressed)
    ProfilePictureUploadResult.UploadFailed -> context.getString(R.string.profile_picture_upload_error_failed)
}