package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.runtime.compositionLocalOf
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.formatExercisePoints
import java.util.Locale

internal enum class QuizReviewOutcome { CORRECT, INCORRECT, WITHHELD, INVALID }
internal val LocalQuizReviewPrecision = compositionLocalOf { 2 }

internal fun multipleChoiceReviewOutcome(question: MultipleChoiceQuizQuestion, option: MultipleChoiceQuizQuestion.AnswerOption): QuizReviewOutcome =
    if (question.invalid || option.invalid) QuizReviewOutcome.INVALID else outcome(option.isCorrect)

internal fun shortAnswerReviewCorrectness(answer: ShortAnswerSubmittedAnswer): Map<Int, Boolean?> =
    answer.submittedTexts.mapNotNull { text -> text.spot?.spotNr?.let { it to text.isCorrect } }.toMap()

internal fun shortAnswerReviewOutcome(question: ShortAnswerQuizQuestion, spot: ShortAnswerQuizQuestion.ShortAnswerSpot, isCorrect: Boolean?): QuizReviewOutcome =
    if (question.invalid || spot.invalid) QuizReviewOutcome.INVALID else outcome(isCorrect)

private fun outcome(correct: Boolean?): QuizReviewOutcome = when (correct) {
    true -> QuizReviewOutcome.CORRECT
    false -> QuizReviewOutcome.INCORRECT
    null -> QuizReviewOutcome.WITHHELD
}

/** All released allowed pairs determine correctness, independently of the chosen sample assignment. */
internal fun dragAndDropReviewOutcomes(
    question: DragAndDropQuizQuestion,
    submitted: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
): Map<Long, QuizReviewOutcome> = question.dropLocations.associate { location ->
    val selected = submitted.entries.firstOrNull { it.key.id == location.id }?.value
    val selectedItem = question.dragItems.firstOrNull { it.id == selected?.id } ?: selected
    val pairs = question.correctMappings?.filter { it.dropLocation?.id == location.id }
    val matching = pairs?.filter { it.dragItem?.id == selected?.id }.orEmpty()
    val status = when {
        question.invalid || location.invalid || selectedItem?.invalid == true || matching.any { it.invalid } -> QuizReviewOutcome.INVALID
        pairs == null -> QuizReviewOutcome.WITHHELD
        selected == null -> if (pairs.isEmpty()) QuizReviewOutcome.CORRECT else QuizReviewOutcome.INCORRECT
        matching.isNotEmpty() -> QuizReviewOutcome.CORRECT
        else -> QuizReviewOutcome.INCORRECT
    }
    location.id to status
}

/** Bipartite matching chooses a valid one-use sample without mutating questions or submitted answers. */
internal fun dragAndDropReviewSample(question: DragAndDropQuizQuestion): Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>? {
    val released = question.correctMappings ?: return null
    if (question.invalid) return emptyMap()
    val items = question.dragItems.filterNot { it.invalid }.associateBy { it.id }
    val locations = question.dropLocations.filterNot { it.invalid }.associateBy { it.id }
    val allowed = released.filterNot { it.invalid }.mapNotNull { pair ->
        val location = pair.dropLocation?.id?.takeIf { it in locations } ?: return@mapNotNull null
        val item = pair.dragItem?.id?.takeIf { it in items } ?: return@mapNotNull null
        location to item
    }.groupBy({ it.first }, { it.second })
    val owner = mutableMapOf<Long, Long>()
    fun assign(location: Long, visited: MutableSet<Long>): Boolean {
        for (item in allowed[location].orEmpty()) {
            if (!visited.add(item)) continue
            val previous = owner[item]
            if (previous == null || assign(previous, visited)) { owner[item] = location; return true }
        }
        return false
    }
    for (location in allowed.keys.sorted()) if (!assign(location, mutableSetOf())) return null
    return owner.entries.associate { (item, location) -> locations.getValue(location) to items.getValue(item) }
}

internal data class QuizReviewScore(val points: String?, val maximum: String?, val percentage: String?)
internal fun quizReviewScore(points: Double?, maximum: Double, precision: Int, locale: Locale = Locale.getDefault()): QuizReviewScore {
    val knownPoints = points?.takeIf { it.isFinite() }
    val percent = if (knownPoints != null && maximum.isFinite() && maximum > 0) knownPoints / maximum * 100 else null
    return QuizReviewScore(
        knownPoints?.let { formatExercisePoints(it, precision, locale) },
        maximum.takeIf { it.isFinite() }?.let { formatExercisePoints(it, precision, locale) },
        percent?.let { formatExercisePoints(it, precision, locale) + "%" },
    )
}

/** Use the server's published maximum; invalidated questions do not create extra possible points. */
internal fun quizReviewMaximum(exercise: de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise): Double =
    exercise.maxPoints?.toDouble() ?: exercise.quizQuestions.filterNot { it.invalid }.let { questions ->
        if (questions.any { it.points == null }) Double.NaN else questions.sumOf { it.points ?: 0.0 }
    }
