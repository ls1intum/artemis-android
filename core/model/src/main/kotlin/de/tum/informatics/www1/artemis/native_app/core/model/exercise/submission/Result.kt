package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val id: Long? = null,
    val completionDate: Instant? = null,
    val successful: Boolean? = null,
    val hasFeedback: Boolean? = null,
    /**
     * Current score in percent i.e. between 1 - 100
     * - Can be larger than 100 if bonus points are available
     */
    val score: Float? = null,
    val assessmentType: Exercise.AssessmentType? = null,
    val rated: Boolean? = null,
    val hasComplaint: Boolean? = null,
    val exampleResult: Boolean? = null,
    val testCaseCount: Int? = null,
    val passedTestCaseCount: Int? = null,
    val codeIssueCount: Int? = null,
    val submission: Submission? = null,
    val assessor: User? = null,
    val participation: Participation? = null
)
