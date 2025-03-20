package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InclusionList(
    val students: Boolean = true,
    val tutors: Boolean = true,
    val instructors: Boolean = true
) : Parcelable
