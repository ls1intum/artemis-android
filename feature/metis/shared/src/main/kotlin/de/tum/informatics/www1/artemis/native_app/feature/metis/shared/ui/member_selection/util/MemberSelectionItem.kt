package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common.getChannelIconImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class MemberSelectionItem: Parcelable {
    abstract val humanReadableName: String

    data class Recipient(override val humanReadableName: String, val username: String, val imageUrl: String, val userId: Long) : MemberSelectionItem()

    data class Conversation(override val humanReadableName: String, val id: Long, val imageVector: @RawValue ImageVector) : MemberSelectionItem()

    fun getTestTag(): String {
        return when (this) {
            is Recipient -> "selectedRecipient$username"
            is Conversation -> "selectedConversation$id"
        }
    }
}

// This function is used in the tests
fun getTestTagForRecipient(username: String): String = "selectedRecipient$username"

fun CourseUser.toMemberSelectionItem(): MemberSelectionItem.Recipient {
    return MemberSelectionItem.Recipient(
        humanReadableName = humanReadableName,
        username = username ?: "",
        imageUrl = imageUrl ?: "",
        userId = id
    )
}

fun Conversation.toMemberSelectionItem(): MemberSelectionItem.Conversation {
    return MemberSelectionItem.Conversation(
        humanReadableName = humanReadableName,
        id = id,
        imageVector = when (this) {
            is ChannelChat -> getChannelIconImageVector(this)
            is GroupChat -> Icons.Default.Groups
            else -> Icons.Default.Numbers
        }
    )
}