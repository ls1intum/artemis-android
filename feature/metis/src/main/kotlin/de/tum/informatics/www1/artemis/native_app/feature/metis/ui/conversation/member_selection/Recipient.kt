package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipient(val username: String, val humanReadableName: String) : Parcelable
