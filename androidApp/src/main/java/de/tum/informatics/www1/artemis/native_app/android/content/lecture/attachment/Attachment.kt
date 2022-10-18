package de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Parcelize
@Serializable
@JsonClassDiscriminator("attachmentType")
sealed class Attachment : Parcelable {
    abstract val id: Int
    abstract val name: String
    abstract val visibleToStudents: Boolean
}