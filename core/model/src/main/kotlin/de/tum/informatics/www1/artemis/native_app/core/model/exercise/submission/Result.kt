package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.ProgrammingExerciseStudentParticipation
import kotlinx.datetime.Clock
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

// This code has been inspired by the following code snippet: https://github.com/ls1intum/artemis-ios-core-modules/blob/23fa0aaf0b16db7643c2589a65bf1e09ff257546/Sources/SharedModels/Exercise/Submission/Result.swift
// This code is available on the artemis-ios-core-modules repository: https://github.com/ls1intum/artemis-ios-core-modules
val Result.isResultPreliminary: Boolean
    get() {
        val exercise = participation?.exercise ?: return false

        when (exercise) {
            is ProgrammingExercise -> {
                when (participation) {
                    is ProgrammingExerciseStudentParticipation -> {
                        if (participation.testRun == true) {
                            return false
                        }
                    }
                    else -> {}
                }

                if (exercise.assessmentType != Exercise.AssessmentType.AUTOMATIC) {
                    val assessmentDueDate = exercise.assessmentDueDate ?: return exercise.assessmentType == Exercise.AssessmentType.AUTOMATIC
                    return assessmentDueDate > Clock.System.now()
                }

                val buildAndTestStudentSubmissionsAfterDueDate = exercise.buildAndTestStudentSubmissionsAfterDueDate ?: return false
                val completionDate = completionDate ?: return false
                return completionDate < buildAndTestStudentSubmissionsAfterDueDate
            }
            else -> {
                return false
            }
        }
    }