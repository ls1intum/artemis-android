package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R

enum class ConversationsOverviewSection(
    val expandedByDefault: Boolean = true,
) {
    FAVOURITES,
    CHANNELS,
    EXERCISES,
    LECTURES,
    EXAMS,
    GROUP_CHATS,
    DIRECT_MESSAGES,
    HIDDEN(expandedByDefault = false);

    val textRes: Int
        get() =  when (this) {
            FAVOURITES -> R.string.conversation_overview_section_favorites
            HIDDEN -> R.string.conversation_overview_section_hidden
            CHANNELS -> R.string.conversation_overview_section_general_channels
            EXERCISES -> R.string.conversation_overview_section_exercise_channels
            LECTURES -> R.string.conversation_overview_section_lecture_channels
            EXAMS -> R.string.conversation_overview_section_exam_channels
            GROUP_CHATS -> R.string.conversation_overview_section_groups
            DIRECT_MESSAGES -> R.string.conversation_overview_section_direct_messages
        }

    val icon: ImageVector
        get() = when (this) {
            FAVOURITES -> Icons.Default.Favorite
            HIDDEN -> Icons.Default.Archive
            CHANNELS -> Icons.Default.ChatBubble
            EXERCISES -> Icons.AutoMirrored.Filled.List
            LECTURES -> Icons.AutoMirrored.Filled.InsertDriveFile
            EXAMS -> Icons.Default.School
            GROUP_CHATS -> Icons.Default.Forum
            DIRECT_MESSAGES -> Icons.AutoMirrored.Filled.Message
        }
}