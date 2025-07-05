package de.tum.informatics.www1.artemis.native_app.core.ui.exercise.util

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlinx.datetime.Instant

object ExerciseParticipationLastRatedUtil {
    fun findLatestRatedResult(participation: Participation?): Result? {
        return participation?.submissions
            ?.flatMap { submission ->
                submission.results
                    ?.filter { it.rated == true }
                    ?.map { it to (it.completionDate ?: Instant.fromEpochSeconds(0L)) }
                    ?: emptyList()
            }
            ?.maxByOrNull { it.second }
            ?.first
            ?: participation?.results.orEmpty()
                .filter { it.rated == true }
                .maxByOrNull { it.completionDate ?: Instant.fromEpochSeconds(0L) }
    }
}