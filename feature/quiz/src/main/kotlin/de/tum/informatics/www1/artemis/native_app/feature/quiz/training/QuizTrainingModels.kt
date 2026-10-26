package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
internal data class TrainingQuestion(
    val quizQuestionWithSolutionDTO: QuizQuestion,
    val isRated: Boolean,
    val questionIds: List<Long>? = null,
    val isNewSession: Boolean = false,
)

/** NON_EMPTY omits a rated empty excluded-ID set; unrated absence has legacy paging semantics. */
internal val TrainingQuestion.sessionQuestionIds: List<Long>?
    get() = questionIds ?: if (isRated) emptyList() else null

internal data class TrainingPage(val questions: List<TrainingQuestion>, val hasNext: Boolean)

internal data class TrainingPageRequest(
    val page: Int = 0,
    val questionIds: List<Long> = emptyList(),
    val isNewSession: Boolean = true,
)

@Serializable internal data class TrainingPrivacy(val showInLeaderboard: Boolean? = null)

@Serializable
internal data class TrainingOverview(
    val hasUserSetSettings: Boolean? = null,
    val currentUserEntry: TrainingEntry? = null,
    val leaderboardEntries: List<TrainingEntry> = emptyList(),
    val currentTime: String? = null,
)

@Serializable
internal data class TrainingEntry(
    val rank: Int? = null,
    val userName: String? = null,
    val score: Double? = null,
    val answeredCorrectly: Int? = null,
    val answeredWrong: Int? = null,
    val totalQuestions: Long? = null,
    val dueDate: String? = null,
    val streak: Int? = null,
    val selectedLeague: Int? = null,
)

internal data class TrainingDraft(
    val options: Set<Long> = emptySet(),
    val texts: Map<Int, String> = emptyMap(),
    val mappings: Map<Long, Long> = emptyMap(),
)

internal enum class TrainingStage {
    HOME,
    LOADING,
    ANSWERING,
    SUBMITTING,
    EVALUATED,
    UNCERTAIN,
    PAGE_ERROR,
    UNRATED_CONFIRMATION,
    SUMMARY,
    RESTART_REQUIRED,
    ACCESS_LOST,
}

internal data class TrainingState(
    val stage: TrainingStage = TrainingStage.HOME,
    val overview: TrainingOverview? = null,
    val privacy: TrainingPrivacy? = null,
    val overviewError: Boolean = false,
    val privacySaving: Boolean = false,
    val current: TrainingQuestion? = null,
    val draft: TrainingDraft = TrainingDraft(),
    val evaluation: SubmittedAnswer? = null,
    val answered: Int = 0,
    val exhausted: Boolean = false,
)

/** Strip solution-bearing properties before constructing any editable native component. */
internal fun trainingEditableQuestion(q: QuizQuestion): QuizQuestion =
    when (q) {
        is MultipleChoiceQuizQuestion ->
            q.copy(
                explanation = null,
                hasCorrectOption = false,
                answerOptions =
                    q.answerOptions.map { it.copy(isCorrect = null, explanation = null) },
            )
        is ShortAnswerQuizQuestion ->
            q.copy(explanation = null, solutions = emptyList(), correctMappings = null)
        is DragAndDropQuizQuestion -> q.copy(explanation = null, correctMappings = null)
    }

/** Live/training uses entity references, unlike the exercise-practice DTO. */
internal fun trainingPayload(q: QuizQuestion, draft: TrainingDraft): JsonObject = buildJsonObject {
    fun ref(id: Long) = buildJsonObject { put("id", id) }
    put("quizQuestion", ref(q.id))
    when (q) {
        is MultipleChoiceQuizQuestion -> {
            put("type", "multiple-choice")
            putJsonArray("selectedOptions") {
                draft.options
                    .filter { id -> q.answerOptions.any { it.id == id } }
                    .forEach { add(ref(it)) }
            }
        }
        is DragAndDropQuizQuestion -> {
            put("type", "drag-and-drop")
            putJsonArray("mappings") {
                draft.mappings.forEach { (drop, item) ->
                    add(
                        buildJsonObject {
                            put("dragItem", ref(item))
                            put("dropLocation", ref(drop))
                        }
                    )
                }
            }
        }
        is ShortAnswerQuizQuestion -> {
            put("type", "short-answer")
            putJsonArray("submittedTexts") {
                q.spots.forEach { spot ->
                    val text = draft.texts[spot.spotNr].orEmpty()
                    if (text.isNotBlank()) {
                        val id = requireNotNull(spot.id)
                        add(
                            buildJsonObject {
                                put("text", text)
                                put("spot", ref(id))
                            }
                        )
                    }
                }
            }
        }
    }
}
