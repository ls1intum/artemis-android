package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.label
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

object ExerciseOverviewChipUtil {

    private const val MAX_NUMBER_OF_CHIPS = 2

    @Composable
    fun buildMainChips(exercise: Exercise): List<OverviewChipData> {
        return buildList {
            // Points
            exercise.maxPoints?.let { max ->
                val currentUserPoints =
                    exercise.currentUserPoints.let(ExercisePointsDecimalFormat::format)
                val maxPoints = max.let(ExercisePointsDecimalFormat::format)

                val pointsText = when {
                    currentUserPoints != null && maxPoints != null -> stringResource(
                        id = R.string.exercise_chips_exercise_points,
                        currentUserPoints,
                        maxPoints
                    )

                    maxPoints != null -> stringResource(
                        id = R.string.exercise_chips_points_max,
                        maxPoints
                    )

                    else -> stringResource(id = R.string.exercise_chips_points_none)
                }

                add(
                    TextChip(
                        title = stringResource(R.string.exercise_chips_points_title),
                        value = pointsText
                    )
                )
            }

            // Due date
            exercise.dueDate?.let { due ->
                val now = Clock.System.now()
                val remaining = due - now

                val isLessThanDay = remaining.isPositive() && remaining < 1.days

                val value = getFormattedSubmissionTime(due)

                val titleRes = if (remaining.isPositive()) {
                    R.string.exercise_chips_submission_due_title
                } else {
                    R.string.exercise_chips_submission_closed_title
                }

                add(
                    TextChip(
                        title = stringResource(titleRes),
                        value = value.orEmpty(),
                        isWarning = isLessThanDay
                    )
                )
            }

            // Participation Status
            add(
                ContentChip(
                    title = stringResource(R.string.exercise_chips_status_title),
                    content = {
                        ProvideDefaultExerciseTemplateStatus(exercise) {
                            de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi(
                                modifier = Modifier,
                                exercise = exercise,
                                isChip = true
                            )
                        }
                    }
                )
            )
        }
    }

    fun buildDifficultyChip(exercise: Exercise): DifficultyChipData? {
        return exercise.difficulty?.let { DifficultyChipData(it) }
    }

    @Composable
    fun buildCategoryChip(exercise: Exercise): CategoryChipData? {
        val badges = buildList {
            // Not released yet
            exercise.releaseDate?.takeIf { it > Clock.System.now() }?.let {
                add(
                    SpecialBadgeData(
                        text = stringResource(R.string.exercise_chips_not_released),
                        background = ExerciseColors.Category.notReleased
                    )
                )
            }
            // Included in overall score
            if (exercise.includedInOverallScore != Exercise.IncludedInOverallScore.INCLUDED_COMPLETELY) {
                add(
                    SpecialBadgeData(
                        text = stringResource(exercise.includedInOverallScore.label),
                        background = ExerciseColors.Type.notIncluded
                    )
                )
            }
            // Categories (max 2 + remainder)
            exercise.categories.take(MAX_NUMBER_OF_CHIPS).forEach { cat ->
                add(
                    SpecialBadgeData(
                        text = cat.category,
                        background = cat.colorValue?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            // "x more" badge
            val remainder = exercise.categories.size - MAX_NUMBER_OF_CHIPS
            if (remainder > 0) {
                add(
                    SpecialBadgeData(
                        text = "+$remainder more",
                        background = Color.LightGray
                    )
                )
            }
        }
        return badges.takeIf { it.isNotEmpty() }?.let { CategoryChipData(it) }
    }
}
