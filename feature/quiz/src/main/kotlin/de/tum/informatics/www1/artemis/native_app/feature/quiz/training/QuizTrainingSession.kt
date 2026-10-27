package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.data.service.StudentReviewHttpException
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject

/**
 * One in-memory session survives native page/layout/lifecycle changes. No persistent answer queue.
 */
internal class QuizTrainingSession(private val api: QuizTrainingApi) {
    private val mutable = MutableStateFlow(TrainingState())
    val state = mutable.asStateFlow()
    private var generation = 0
    private var accessGeneration = 0
    private var request = TrainingPageRequest()
    private var snapshot: List<Long>? = null
    private var snapshotNew = false
    private var legacyUnrated = false
    private var hasNext = false
    private var queue = ArrayDeque<TrainingQuestion>()
    private val seen = mutableSetOf<Long>()
    private var frozen: JsonObject? = null
    private var lastRating: Boolean? = null
    private var summaryAt = 10
    private var overviewLoading = false
    private var privacyRevision = 0

    private fun change(block: (TrainingState) -> TrainingState) {
        mutable.value = block(mutable.value)
    }

    fun invalidate() {
        generation++
        accessGeneration++
        queue.clear()
        seen.clear()
        frozen = null
        snapshot = null
        change { TrainingState(stage = TrainingStage.ACCESS_LOST) }
    }

    suspend fun onOverviewResumed() = loadOverview()

    suspend fun loadOverview() {
        if (overviewLoading || state.value.stage == TrainingStage.ACCESS_LOST) return
        overviewLoading = true
        val gen = accessGeneration
        val revision = privacyRevision
        try {
            val overview = api.loadOverview()
            if (gen != accessGeneration) return
            // The overview GET creates a private entry. Retain its pre-read onboarding flag
            // even when the following settings read fails or observes that initialization.
            change {
                it.copy(
                    overview =
                        overview.copy(
                            hasUserSetSettings =
                                it.overview?.hasUserSetSettings ?: overview.hasUserSetSettings
                        )
                )
            }
            val privacy = api.loadSettings()
            if (gen == accessGeneration)
                change {
                    if (privacyRevision == revision)
                        it.copy(privacy = privacy, overviewError = false)
                    else it
                }
        } catch (e: Exception) {
            if (gen == accessGeneration) handle(e, generation) { it.copy(overviewError = true) }
        } finally {
            overviewLoading = false
        }
    }

    suspend fun updatePrivacy(show: Boolean) {
        if (state.value.privacySaving || state.value.stage == TrainingStage.ACCESS_LOST) return
        val gen = accessGeneration
        privacyRevision++
        change { it.copy(privacySaving = true) }
        try {
            api.updatePrivacy(show)
            if (gen == accessGeneration)
                change {
                    it.copy(
                        privacy = TrainingPrivacy(show),
                        overview = it.overview?.copy(hasUserSetSettings = true),
                        privacySaving = false,
                        overviewError = false,
                    )
                }
        } catch (e: Exception) {
            if (gen == accessGeneration)
                handle(e, generation) { it.copy(privacySaving = false, overviewError = true) }
        }
    }

    suspend fun start() {
        if (
            state.value.stage != TrainingStage.HOME &&
                state.value.stage != TrainingStage.RESTART_REQUIRED &&
                !(state.value.stage == TrainingStage.SUMMARY && state.value.exhausted)
        )
            return
        generation++
        request = TrainingPageRequest()
        snapshot = null
        snapshotNew = false
        legacyUnrated = false
        hasNext = false
        queue.clear()
        seen.clear()
        lastRating = null
        frozen = null
        summaryAt = 10
        change {
            it.copy(
                answered = 0,
                current = null,
                draft = TrainingDraft(),
                evaluation = null,
                exhausted = false,
            )
        }
        fetchPage()
    }

    suspend fun retryPage() {
        if (state.value.stage == TrainingStage.PAGE_ERROR) fetchPage()
    }

    private suspend fun fetchPage() {
        val gen = generation
        change { it.copy(stage = TrainingStage.LOADING) }
        try {
            val result = api.loadPage(request)
            if (gen != generation) return
            if (legacyUnrated && result.questions.any { it.isRated }) {
                queue.clear()
                change {
                    it.copy(
                        stage = TrainingStage.RESTART_REQUIRED,
                        current = null,
                        evaluation = null,
                        draft = TrainingDraft(),
                    )
                }
                return
            }
            require(result.questions.map { it.isRated }.distinct().size <= 1)
            require(result.questions.isNotEmpty() || !result.hasNext)
            val first = result.questions.firstOrNull()
            if (request.page == 0 && first != null) {
                snapshot = first.sessionQuestionIds?.toList()
                snapshotNew = first.isNewSession
                legacyUnrated = !first.isRated && first.questionIds == null
            }
            require(
                result.questions.all {
                    // The API represents the excluded IDs as a set; serialization order is not identity.
                    it.sessionQuestionIds?.toSet() == snapshot?.toSet() &&
                        (legacyUnrated || it.isNewSession == snapshotNew)
                }
            )
            hasNext = result.hasNext
            val unique =
                result.questions
                    .filter { it.quizQuestionWithSolutionDTO.id !in seen }
                    .distinctBy { it.quizQuestionWithSolutionDTO.id }
            require(unique.isNotEmpty() || !hasNext)
            unique.forEach {
                seen += it.quizQuestionWithSolutionDTO.id
                queue.addLast(it)
            }
            request =
                TrainingPageRequest(
                    request.page + 1,
                    snapshot.orEmpty(),
                    if (legacyUnrated) true else snapshotNew,
                )
            presentNext()
        } catch (e: Exception) {
            handle(e, gen) { it.copy(stage = TrainingStage.PAGE_ERROR) }
        }
    }

    private fun presentNext() {
        val q = queue.removeFirstOrNull()
        if (q == null) {
            change {
                it.copy(
                    stage = TrainingStage.SUMMARY,
                    current = null,
                    evaluation = null,
                    draft = TrainingDraft(),
                    exhausted = true,
                )
            }
            return
        }
        frozen = null
        val needsConfirm = !q.isRated && lastRating != false
        lastRating = q.isRated
        change {
            it.copy(
                current = q,
                draft = TrainingDraft(),
                evaluation = null,
                stage =
                    if (needsConfirm) TrainingStage.UNRATED_CONFIRMATION
                    else TrainingStage.ANSWERING,
            )
        }
    }

    fun acceptUnrated() {
        if (state.value.stage == TrainingStage.UNRATED_CONFIRMATION)
            change { it.copy(stage = TrainingStage.ANSWERING) }
    }

    fun finish() {
        if (state.value.stage != TrainingStage.ACCESS_LOST)
            change {
                it.copy(
                    stage = TrainingStage.SUMMARY,
                    exhausted = it.evaluation != null && queue.isEmpty() && !hasNext,
                )
            }
    }

    fun selectOption(id: Long, selected: Boolean) {
        if (state.value.stage != TrainingStage.ANSWERING) return
        val q =
            state.value.current?.quizQuestionWithSolutionDTO as? MultipleChoiceQuizQuestion
                ?: return
        if (q.answerOptions.none { it.id == id }) return
        change {
            it.copy(
                draft =
                    it.draft.copy(
                        options =
                            if (selected) {
                                if (q.singleChoice) setOf(id) else it.draft.options + id
                            } else it.draft.options - id
                    )
            )
        }
    }

    fun setText(spot: Int, text: String) {
        if (state.value.stage != TrainingStage.ANSWERING) return
        val question =
            state.value.current?.quizQuestionWithSolutionDTO as? ShortAnswerQuizQuestion ?: return
        if (question.spots.none { it.spotNr == spot && it.id != null }) return
        change { it.copy(draft = it.draft.copy(texts = it.draft.texts + (spot to text))) }
    }

    fun moveItem(item: Long, drop: Long) {
        if (state.value.stage != TrainingStage.ANSWERING) return
        val question =
            state.value.current?.quizQuestionWithSolutionDTO as? DragAndDropQuizQuestion ?: return
        if (item !in question.dragItemById || drop !in question.dropLocationById) return
        change {
            it.copy(
                draft =
                    it.draft.copy(
                        mappings =
                            it.draft.mappings.filterValues { value -> value != item } +
                                (drop to item)
                    )
            )
        }
    }

    fun clearDrop(drop: Long) {
        if (state.value.stage == TrainingStage.ANSWERING)
            change { it.copy(draft = it.draft.copy(mappings = it.draft.mappings - drop)) }
    }

    fun swapDrops(old: Long, new: Long) {
        if (state.value.stage != TrainingStage.ANSWERING) return
        val question =
            state.value.current?.quizQuestionWithSolutionDTO as? DragAndDropQuizQuestion ?: return
        if (old !in question.dropLocationById || new !in question.dropLocationById) return
        val mappings = state.value.draft.mappings
        val item = mappings[old] ?: return
        val other = mappings[new]
        change {
            it.copy(
                draft =
                    it.draft.copy(
                        mappings =
                            (mappings - old - new) +
                                (new to item) +
                                (if (other != null) mapOf(old to other) else emptyMap())
                    )
            )
        }
    }

    suspend fun submit() {
        if (state.value.stage != TrainingStage.ANSWERING) return
        val q = state.value.current ?: return
        frozen = trainingPayload(q.quizQuestionWithSolutionDTO, state.value.draft)
        sendFrozen()
    }

    suspend fun retrySubmission() {
        if (state.value.stage == TrainingStage.UNCERTAIN) sendFrozen()
    }

    private suspend fun sendFrozen() {
        val q = state.value.current ?: return
        val payload = frozen ?: return
        val gen = generation
        change { it.copy(stage = TrainingStage.SUBMITTING) }
        try {
            val evaluated = api.submit(q, payload)
            if (
                gen != generation ||
                    state.value.current?.quizQuestionWithSolutionDTO?.id !=
                        q.quizQuestionWithSolutionDTO.id
            )
                return
            require(evaluated.quizQuestion?.id == q.quizQuestionWithSolutionDTO.id)
            require(evaluated.quizQuestion!!::class == q.quizQuestionWithSolutionDTO::class)
            change {
                it.copy(
                    stage = TrainingStage.EVALUATED,
                    evaluation = evaluated,
                    answered = it.answered + 1,
                )
            }
        } catch (e: Exception) {
            handle(e, gen) { it.copy(stage = TrainingStage.UNCERTAIN) }
        }
    }

    suspend fun skipQuestion() {
        if (state.value.stage == TrainingStage.ANSWERING) advance()
    }

    suspend fun next() {
        if (state.value.stage != TrainingStage.EVALUATED) return
        if (state.value.answered >= summaryAt) {
            change {
                it.copy(stage = TrainingStage.SUMMARY, exhausted = queue.isEmpty() && !hasNext)
            }
            return
        }
        advance()
    }

    suspend fun continueTraining() {
        if (state.value.stage != TrainingStage.SUMMARY || state.value.exhausted) return
        summaryAt = state.value.answered + 10
        // An early Finish retains an unanswered or uncertain question exactly as it was.
        if (state.value.current != null && state.value.evaluation == null) {
            change {
                it.copy(
                    stage =
                        if (frozen != null) TrainingStage.UNCERTAIN
                        else if (it.current?.isRated == false && lastRating != false)
                            TrainingStage.UNRATED_CONFIRMATION
                        else TrainingStage.ANSWERING
                )
            }
            return
        }
        advance()
    }

    private suspend fun advance() {
        if (queue.isNotEmpty()) presentNext()
        else if (hasNext) fetchPage()
        else {
            change { it.copy(stage = TrainingStage.SUMMARY, exhausted = true) }
        }
    }

    private fun handle(e: Exception, gen: Int, failure: (TrainingState) -> TrainingState) {
        if (e is CancellationException) throw e
        if (gen != generation) return
        if (e is StudentReviewHttpException && e.status in listOf(401, 403)) invalidate()
        else change(failure)
    }
}
