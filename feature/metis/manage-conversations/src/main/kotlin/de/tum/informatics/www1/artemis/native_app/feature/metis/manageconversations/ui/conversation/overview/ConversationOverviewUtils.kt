package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

object ConversationOverviewUtils {

    private const val FILTER_EXERCISE = "exercise"
    private const val FILTER_LECTURE = "lecture"

    fun isRecent(conversation: Conversation, course: Course?): Boolean {
        if (conversation !is ChannelChat) return false

        val now = Clock.System.now()
        val startDateRange = now.minus(10.days)
        val endDateRange = now.plus(10.days)

        val exercise = course?.exercises?.firstOrNull { it.id == conversation.subTypeReferenceId }
        val lecture = course?.lectures?.firstOrNull { it.id == conversation.subTypeReferenceId }

        val exerciseStart = exercise?.releaseDate ?: Instant.DISTANT_PAST
        val exerciseEnd = exercise?.dueDate ?: Instant.DISTANT_FUTURE

        val lectureStart = lecture?.startDate ?: Instant.DISTANT_PAST
        val lectureEnd = lecture?.endDate ?: Instant.DISTANT_FUTURE

        return when {
            conversation.filterPredicate(FILTER_EXERCISE) ->
                exerciseStart in startDateRange..endDateRange || exerciseEnd in startDateRange..endDateRange

            conversation.filterPredicate(FILTER_LECTURE) ->
                lectureStart in startDateRange..endDateRange || lectureEnd in startDateRange..endDateRange

            else -> false
        }
    }

    sealed class ConversationFilter(
        val titleId: Int,
        val icon: ImageVector,
        val id: Int,
        val selectedColor: Color,
        val selectedIcon: ImageVector = icon,
        val onClick : () -> Unit = {}
    ) {
        data object All : ConversationFilter(R.string.conversation_overview_filter_all, Icons.Default.AllInbox, 1,  ComponentColors.ChannelFilter.all)
        data object Unread : ConversationFilter(R.string.conversation_overview_filter_unread, Icons.Outlined.MarkChatUnread, 2, ComponentColors.ChannelFilter.unread, Icons.Default.MarkChatUnread)
        data object Recent : ConversationFilter(R.string.conversation_overview_filter_recent, Icons.Default.AccessTime, 3, ComponentColors.ChannelFilter.recent, Icons.Default.AccessTimeFilled)
        data object Unresolved : ConversationFilter(R.string.conversation_overview_filter_unresolved, Icons.Default.QuestionMark, 4, ComponentColors.ChannelFilter.unresolved)
    }
}