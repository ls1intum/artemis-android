package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InclusionList(
    val students: Boolean = true,
    val tutors: Boolean = true,
    val instructors: Boolean = true
) : Parcelable
