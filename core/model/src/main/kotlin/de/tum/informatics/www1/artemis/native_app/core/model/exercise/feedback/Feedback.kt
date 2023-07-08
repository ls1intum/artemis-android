package de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Feedback(
    val id: Int? = null,
    val gradingInstruction: GradingInstruction? = null,
    val text: String? = null,
    val detailText: String? = null,
    val reference: String? = null,
    val credits: Float? = null,
    val type: FeedbackCreationType? = null,
    val result: Result? = null,
    val positive: Boolean? = null,
    val conflictingTextAssessments: List<FeedbackConflict>? = null,
    val suggestedFeedbackReference: String? = null,
    val suggestedFeedbackOriginSubmissionReference: Int? = null,
    val suggestedFeedbackParticipationReference: Int? = null,

    // Specifies whether or not the tutor feedback is correct relative to the instructor feedback (during tutor training) or if there is a validation error.
    // Client only property.
    // val correctionStatus: FeedbackCorrectionStatus?,
) {
    private companion object {
        private const val SUBMISSION_POLICY_FEEDBACK_IDENTIFIER = "SubPolFeedbackIdentifier:"
        private const val STATIC_CODE_ANALYSIS_FEEDBACK_IDENTIFIER = "SCAFeedbackIdentifier:"
    }

    val isSubmissionPolicy: Boolean
        get() {
            return type == FeedbackCreationType.AUTOMATIC && text.orEmpty().contains(
                SUBMISSION_POLICY_FEEDBACK_IDENTIFIER
            )
        }

    val submissionPolicyTitle: String
        get() {
            return if (isSubmissionPolicy) {
                text.orEmpty().substring(SUBMISSION_POLICY_FEEDBACK_IDENTIFIER.length)
            } else ""
        }

    val isStaticCodeAnalysis: Boolean
        get() {
            return type == FeedbackCreationType.AUTOMATIC && text.orEmpty().contains(
                STATIC_CODE_ANALYSIS_FEEDBACK_IDENTIFIER
            )
        }

    val scaCategory: String
        get() {
            return if (isStaticCodeAnalysis) {
                text.orEmpty().substring(STATIC_CODE_ANALYSIS_FEEDBACK_IDENTIFIER.length)
            } else ""
        }

    val staticCodeAnalysisIssue: StaticCodeAnalysisIssue
        get() {
            check(isStaticCodeAnalysis) { "This feedback is not of static analysis type" }
            return Json.decodeFromString(detailText ?: "")
        }

    val isTestCase: Boolean
        get() {
            return type == FeedbackCreationType.AUTOMATIC &&
                    (text == null
                            || (
                            !text.contains(SUBMISSION_POLICY_FEEDBACK_IDENTIFIER) && !text.contains(
                                STATIC_CODE_ANALYSIS_FEEDBACK_IDENTIFIER
                            )))
        }

    enum class FeedbackCreationType {
        AUTOMATIC,
        MANUAL,
        MANUAL_UNREFERENCED,
        AUTOMATIC_ADAPTED
    }

    /**
     * Possible tutor feedback states upon validation from the server.
     */
    enum class FeedbackCorrectionErrorType {
        INCORRECT_SCORE,
        UNNECESSARY_FEEDBACK,
        MISSING_GRADING_INSTRUCTION,
        INCORRECT_GRADING_INSTRUCTION,
        EMPTY_NEGATIVE_FEEDBACK
    }
}