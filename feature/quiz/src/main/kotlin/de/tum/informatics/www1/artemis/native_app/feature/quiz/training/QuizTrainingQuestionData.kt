package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.*
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData

internal fun trainingResultData(answer: SubmittedAnswer): QuizQuestionData<*> {
    return when (val q = requireNotNull(answer.quizQuestion)) {
        is MultipleChoiceQuizQuestion -> {
            val selected =
                (answer as MultipleChoiceSubmittedAnswer).selectedOptions.map { it.id }.toSet()
            QuizQuestionData.MultipleChoiceData.Result(
                q,
                q.answerOptions.associate { it.id to (it.id in selected) },
                answer.scoreInPoints,
            )
        }
        is ShortAnswerQuizQuestion -> {
            val texts =
                (answer as ShortAnswerSubmittedAnswer)
                    .submittedTexts
                    .mapNotNull { t ->
                        val nr =
                            q.spots.firstOrNull { it.id == t.spot?.id }?.spotNr
                                ?: t.spot?.spotNr
                                ?: return@mapNotNull null
                        nr to t.text.orEmpty()
                    }
                    .toMap()
            val correctness =
                answer.submittedTexts
                    .mapNotNull { t ->
                        val nr =
                            q.spots.firstOrNull { it.id == t.spot?.id }?.spotNr
                                ?: t.spot?.spotNr
                                ?: return@mapNotNull null
                        nr to t.isCorrect
                    }
                    .toMap()
            QuizQuestionData.ShortAnswerData.Result(q, texts, answer.scoreInPoints, correctness)
        }
        is DragAndDropQuizQuestion -> {
            val mappings =
                (answer as DragAndDropSubmittedAnswer)
                    .mappings
                    .mapNotNull { m ->
                        val drop =
                            q.dropLocations.firstOrNull { it.id == m.dropLocation?.id }
                                ?: return@mapNotNull null
                        val item =
                            q.dragItems.firstOrNull { it.id == m.dragItem?.id }
                                ?: return@mapNotNull null
                        drop to item
                    }
                    .toMap()
            QuizQuestionData.DragAndDropData.Result(
                q,
                q.dragItems.filter { item -> mappings.values.none { it.id == item.id } },
                mappings,
                answer.scoreInPoints,
            )
        }
    }
}

internal fun trainingEditableData(
    state: TrainingState,
    session: QuizTrainingSession,
): QuizQuestionData<*> =
    when (
        val q = trainingEditableQuestion(requireNotNull(state.current).quizQuestionWithSolutionDTO)
    ) {
        is MultipleChoiceQuizQuestion ->
            QuizQuestionData.MultipleChoiceData.Editable(
                q,
                q.answerOptions.associate { it.id to (it.id in state.draft.options) },
                session::selectOption,
            )
        is ShortAnswerQuizQuestion ->
            QuizQuestionData.ShortAnswerData.Editable(q, state.draft.texts, session::setText)
        is DragAndDropQuizQuestion -> {
            val mappings =
                state.draft.mappings
                    .mapNotNull { (drop, item) ->
                        q.dropLocationById[drop]?.let { d -> q.dragItemById[item]?.let { d to it } }
                    }
                    .toMap()
            QuizQuestionData.DragAndDropData.Editable(
                q,
                q.dragItems.filter { it.id !in state.draft.mappings.values },
                mappings,
                session::moveItem,
                session::swapDrops,
                session::clearDrop,
            )
        }
    }

internal fun trainingQuestionSupported(q: QuizQuestion): Boolean =
    when (q) {
        is MultipleChoiceQuizQuestion ->
            q.answerOptions.isNotEmpty() &&
                q.answerOptions.map { it.id }.distinct().size == q.answerOptions.size
        is ShortAnswerQuizQuestion ->
            q.spots.isNotEmpty() &&
                q.spots.all { it.id != null && it.spotNr != null } &&
                q.spots.map { it.spotNr }.distinct().size == q.spots.size
        is DragAndDropQuizQuestion ->
            q.dropLocations.isNotEmpty() &&
                q.dropLocations.all { d ->
                    listOf(d.posX, d.posY, d.width, d.height).all {
                        it != null && it.isFinite() && it >= 0
                    } && (d.width ?: 0.0) > 0 && (d.height ?: 0.0) > 0
                }
    }
