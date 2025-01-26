package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
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
}