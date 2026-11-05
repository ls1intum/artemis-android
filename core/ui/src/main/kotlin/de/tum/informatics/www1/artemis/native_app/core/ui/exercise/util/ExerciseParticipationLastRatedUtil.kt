package de.tum.informatics.www1.artemis.native_app.core.ui.exercise.util

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlin.time.Instant

object ExerciseParticipationLastRatedUtil {
    fun findLatestRatedResult(participation: Participation?): Result? {
        return (participation?.submissions.orEmpty().flatMap { it.results.orEmpty() } + participation?.results.orEmpty())
            .filter { it.rated == true }
            .maxWithOrNull(compareBy<Result> { it.completionDate ?: Instant.fromEpochSeconds(0L) }.thenBy { it.id })
    }
}