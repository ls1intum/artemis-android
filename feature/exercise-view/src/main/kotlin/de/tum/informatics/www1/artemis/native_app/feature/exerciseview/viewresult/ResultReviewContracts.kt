package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val diagnosticJson = Json { ignoreUnknownKeys = true }

/** Deliberately allowlisted student contract. No rubric, assessor notes or grading configuration. */
@Serializable
internal data class ReviewFeedback(
    val id: Long? = null,
    val text: String? = null,
    val detailText: String? = null,
    val hasLongFeedbackText: Boolean = false,
    val reference: String? = null,
    val credits: Double? = null,
    val type: String? = null,
    val positive: Boolean? = null,
    val testCase: ReviewTestCase? = null,
    private val gradingInstruction: PublicFeedbackMessage? = null,
) {
    val group: FeedbackGroup get() = when {
        type == "AUTOMATIC" && text.orEmpty().startsWith("SCAFeedbackIdentifier:") -> FeedbackGroup.STATIC_ANALYSIS
        type == "AUTOMATIC" && text.orEmpty().startsWith("SubPolFeedbackIdentifier:") -> FeedbackGroup.POLICY
        type == "AUTOMATIC" && testCase != null -> FeedbackGroup.TESTS
        else -> FeedbackGroup.MANUAL
    }
    val title: String? get() = testCase?.testName ?: text
        ?.removePrefix("SCAFeedbackIdentifier:")
        ?.removePrefix("SubPolFeedbackIdentifier:")
        ?.replace(Regex("^(?:NonGraded)?FeedbackSuggestion:(?:(?:accepted|adapted):)?"), "")
        ?.takeIf { it.isNotBlank() }
    val diagnostic: ReviewDiagnostic? get() = if (group == FeedbackGroup.STATIC_ANALYSIS) {
        runCatching { diagnosticJson.decodeFromString<ReviewDiagnostic>(detailText.orEmpty()) }.getOrNull()
    } else null
    val message: String get() = listOfNotNull(gradingInstruction?.feedback, diagnostic?.message ?: detailText)
        .filter { it.isNotBlank() }.joinToString("\n")
    val previewMessage: String get() = message.let { if (hasLongFeedbackText && it.length > 500) it.take(500) + "…" else it }
    // Only the public standardized message is retained, even if the server accidentally includes the full grading instruction.
    @Serializable data class PublicFeedbackMessage(val feedback: String? = null)
}

internal enum class FeedbackGroup { TESTS, STATIC_ANALYSIS, POLICY, MANUAL }
@Serializable internal data class ReviewTestCase(val testName: String? = null)
@Serializable internal data class ReviewDiagnostic(
    val filePath: String? = null, val startLine: Int? = null, val endLine: Int? = null,
    val message: String? = null, val rule: String? = null, val category: String? = null,
    val priority: String? = null, val penalty: Double? = null,
)
@Serializable internal data class ReviewTextBlock(val id: String? = null, val startIndex: Int? = null, val endIndex: Int? = null)
@Serializable internal data class ReviewResult(val id: Long? = null, val feedbacks: List<ReviewFeedback> = emptyList())
@Serializable internal data class ReviewSubmission(
    val id: Long? = null,
    val text: String? = null,
    val model: String? = null,
    val explanationText: String? = null,
    val filePath: String? = null,
    val commitHash: String? = null,
    val submissionDate: String? = null,
    val blocks: List<ReviewTextBlock> = emptyList(),
    val results: List<ReviewResult> = emptyList(),
) {
    /** Offsets are JS/Java UTF-16 code units, applied to the untouched submitted answer. */
    fun rangeFor(feedback: ReviewFeedback): IntRange? {
        val answer = text ?: return null
        val reference = feedback.reference?.takeIf { it.isNotEmpty() } ?: return null
        val block = blocks.firstOrNull { it.id == reference }
        val range = when {
            block != null -> (block.startIndex ?: return null) until (block.endIndex ?: return null)
            reference.matches(Regex("[a-fA-F0-9]{40}")) -> return null
            reference.all { it.isDigit() } -> {
                val start = reference.toIntOrNull() ?: return null
                if (start == Int.MAX_VALUE) return null
                start until start + 1
            }
            else -> {
                val start = answer.indexOf(reference)
                if (start < 0) return null
                start until start + reference.length
            }
        }
        return range.takeIf { it.first >= 0 && !it.isEmpty() && it.last < answer.length }
    }
}
@Serializable internal data class ReviewParticipation(val id: Long? = null, val submissions: List<ReviewSubmission> = emptyList())
@Serializable internal data class ReviewBuildLog(val time: String? = null, val log: String? = null)

internal data class ReviewTarget(val participationId: Long, val submissionId: Long?, val result: Result, val submission: Submission?)

/** A selected result is resolved together with its own submitted work; never with the latest submission. */
internal fun reviewTargets(exercise: Exercise): List<ReviewTarget> {
    val candidates = exercise.studentParticipations.orEmpty().flatMap { participation ->
        val pid = participation.id ?: return@flatMap emptyList()
        val nested = participation.submissions.orEmpty().flatMap { submission ->
            submission.results.orEmpty().map { ReviewTarget(pid, submission.id?.toLong(), it, submission) }
        }
        val direct = participation.results.orEmpty().map { result ->
            val source = result.submission ?: participation.submissions.orEmpty().firstOrNull { s -> s.results.orEmpty().any { it.id == result.id } }
            ReviewTarget(pid, source?.id?.toLong(), result, source)
        }
        nested + direct
    }.filter { it.result.id != null }.distinctBy { it.participationId to it.result.id }
    return candidates.sortedWith(compareByDescending<ReviewTarget> { it.result.completionDate }.thenByDescending { it.result.id })
}

internal fun selectReviewTarget(exercise: Exercise, resultId: Long? = null): ReviewTarget? =
    reviewTargets(exercise).let { candidates -> if (resultId != null) candidates.firstOrNull { it.result.id == resultId } else candidates.firstOrNull() }


internal fun reviewScore(score: Float?, maxPoints: Float?, precision: Int): String? =
    de.tum.informatics.www1.artemis.native_app.core.ui.exercise.formatExerciseResultScore(score, maxPoints, precision)?.let { formatted ->
        if (formatted.points != null && formatted.maximum != null) "${formatted.percentage} · ${formatted.points} / ${formatted.maximum}" else formatted.percentage
    }

/** Only origin-local, served-file URLs may receive the captured account cookie. */
internal fun reviewFileUrl(serverUrl: String, filePath: String?): String? = runCatching {
    val path = filePath?.takeIf { it.isNotBlank() } ?: return null
    if (path.startsWith("//") || path.contains('\\')) return null
    val base = java.net.URI(serverUrl.trimEnd('/') + "/")
    val input = java.net.URI(path)
    val served = if (input.isAbsolute) input else {
        val relative = path.removePrefix("/")
        base.resolve(if (relative.startsWith("api/core/files/") || relative.startsWith("public/")) relative else "api/core/files/$relative")
    }
    if (served.scheme !in listOf("http", "https") || served.scheme != base.scheme || served.host != base.host || served.port != base.port || served.userInfo != null) return null
    if (served.normalize().path != served.path || served.rawPath.contains("%2e", true) || served.rawPath.contains("%2f", true)) return null
    if (!served.path.startsWith("/api/core/files/") && !served.path.startsWith("/public/")) return null
    served.toASCIIString()
}.getOrNull()

internal enum class ReviewAssessmentState { FINAL, PRELIMINARY, AI_PENDING, AI_FAILED, AI_TIMED_OUT }

internal fun reviewAssessmentState(exercise: Exercise, result: Result, practice: Boolean, now: kotlin.time.Instant): ReviewAssessmentState {
    if (result.assessmentType == Exercise.AssessmentType.AUTOMATIC_ATHENA) return when (result.successful) {
        true -> ReviewAssessmentState.PRELIMINARY
        false -> ReviewAssessmentState.AI_FAILED
        null -> if (result.completionDate?.let { it < now } == true) ReviewAssessmentState.AI_TIMED_OUT else ReviewAssessmentState.AI_PENDING
    }
    if (exercise !is de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise || practice) return ReviewAssessmentState.FINAL
    val completion = result.completionDate ?: return ReviewAssessmentState.PRELIMINARY
    val preliminary = if (exercise.assessmentType != Exercise.AssessmentType.AUTOMATIC) {
        when (result.assessmentType) {
            Exercise.AssessmentType.MANUAL, Exercise.AssessmentType.SEMI_AUTOMATIC -> false
            else -> exercise.assessmentDueDate?.let { now < it } ?: (result.assessmentType == Exercise.AssessmentType.AUTOMATIC)
        }
    } else exercise.buildAndTestStudentSubmissionsAfterDueDate?.let { completion < it } ?: false
    return if (preliminary) ReviewAssessmentState.PRELIMINARY else ReviewAssessmentState.FINAL
}

internal fun reviewDate(date: String?): String? = date?.let { raw ->
    runCatching {
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
            .format(java.util.Date(kotlin.time.Instant.parse(raw).toEpochMilliseconds()))
    }.getOrNull()
}

internal fun reviewExampleSolutionUrl(serverUrl: String, courseId: Long, exerciseId: Long): String =
    io.ktor.http.URLBuilder(serverUrl).apply {
        parameters.clear()
        fragment = ""
        appendPathSegments("courses", courseId.toString(), "exercises", exerciseId.toString())
    }.buildString()
