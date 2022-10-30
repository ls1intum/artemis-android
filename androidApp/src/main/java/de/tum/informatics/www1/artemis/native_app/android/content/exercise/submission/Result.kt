package de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission

import de.tum.informatics.www1.artemis.native_app.android.content.account.User
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Result(
    val id: Int? = null,
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
    //val feedbacks: List<Feedback>? = null,
    val participation: Participation? = null
)