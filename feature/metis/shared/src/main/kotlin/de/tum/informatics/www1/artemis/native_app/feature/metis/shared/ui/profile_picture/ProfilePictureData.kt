package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

sealed class ProfilePictureData {

    companion object {
        fun create(username: String, imageUrl: String?): ProfilePictureData {
            val fallBack = InitialsPlaceholder(username)
            return if (imageUrl != null) {
                Image(imageUrl, fallBack)
            } else {
                fallBack
            }
        }
    }

    data class InitialsPlaceholder(val username: String) : ProfilePictureData()
    data class Image(val url: String, val fallBack: InitialsPlaceholder) : ProfilePictureData()
}