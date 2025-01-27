package de.tum.informatics.www1.artemis.native_app.core.ui.exercise.util

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.isResultPreliminary
import de.tum.informatics.www1.artemis.native_app.core.ui.R

// This has been inspired by the Artemis iOS app result string resolving
// https://github.com/ls1intum/artemis-ios/blob/21e152e6d79affd160203e75eb12d050c44090f3/ArtemisKit/Sources/CourseView/ExerciseTab/SubmissionResultView.swift
object ExerciseResultUtil {

    fun resolveScoreString(
        exercise: Exercise,
        formattedPercentage: String,
        result: Result,
        points: String,
        context: Context,
        showPoints: Boolean
    ): String {
        return if (exercise is ProgrammingExercise) {
            ProgrammingExerciseResultUtil.resolveProgrammingExerciseMessage(
                formattedPercentage,
                result,
                points,
                context,
                showPoints
            )
        } else {
            if (showPoints) {
                context.getString(
                    R.string.exercise_result_has_result_score,
                    formattedPercentage,
                    points
                )
            } else {
                context.getString(
                    R.string.exercise_result_has_result_score_without_points,
                    formattedPercentage
                )
            }
        }
    }

    object ProgrammingExerciseResultUtil {

        private const val MAX_PROGRAMMING_RESULT_INTS = 255

        // To enable longer messages including details such as number of passed test cases, etc. set this to true
        private const val SHORT: Boolean = true

        fun resolveProgrammingExerciseMessage(
            score: String,
            result: Result,
            points: String,
            context: Context,
            showPoints: Boolean
        ): String {
            val submission = result.submission as? ProgrammingSubmission
            val testCaseCount = result.testCaseCount ?: 0
            val codeIssueCount = result.codeIssueCount ?: 0
            val passedTestCaseCount = result.passedTestCaseCount ?: 0

            val buildInformation = resolveBuildInformation(context, submission, testCaseCount, passedTestCaseCount)

            val resultString = when {
                SHORT -> context.getString(
                    if (testCaseCount > 0) R.string.exercise_result_has_result_score_without_points
                    else R.string.exercise_result_programming_short_with_build_information,
                    score,
                    buildInformation.takeIf { testCaseCount == 0 }
                )
                codeIssueCount > 0 -> resolveCodeIssuesString(
                    score, buildInformation, codeIssueCount, points, context, showPoints
                )
                else -> resolveNoCodeIssuesString(score, buildInformation, points, context, showPoints)
            }

            return if (result.isResultPreliminary) {
                "$resultString ${context.getString(R.string.exercise_result_is_preliminary)}"
            } else {
                resultString
            }
        }

        private fun resolveBuildInformation(
            context: Context,
            submission: ProgrammingSubmission?,
            testCaseCount: Int,
            passedTestCaseCount: Int
        ): String {
            return when {
                submission?.buildFailed == true -> context.getString(R.string.exercise_result_build_failed)
                testCaseCount < 1 -> context.getString(R.string.exercise_result_build_successful_no_tests)
                else -> {
                    val passedTestCaseCountString = coerceIntToString(passedTestCaseCount)
                    val testCaseCountString = coerceIntToString(testCaseCount)
                    context.getString(
                        R.string.exercise_result_build_successful_tests,
                        passedTestCaseCountString,
                        testCaseCountString
                    )
                }
            }
        }

        private fun resolveCodeIssuesString(
            score: String,
            buildInformation: String,
            codeIssueCount: Int,
            points: String,
            context: Context,
            showPoints: Boolean
        ): String {
            val codeIssueCountString = coerceIntToString(codeIssueCount)

            return if (showPoints) {
                context.getString(
                    R.string.exercise_result_code_issues,
                    score,
                    buildInformation,
                    codeIssueCountString,
                    points
                )
            } else {
                context.getString(
                    R.string.exercise_result_code_issues_without_points,
                    score,
                    buildInformation,
                    codeIssueCountString
                )
            }
        }

        private fun resolveNoCodeIssuesString(
            score: String,
            buildInformation: String,
            points: String,
            context: Context,
            showPoints: Boolean
        ): String {
            return if (showPoints) {
                context.getString(
                    R.string.exercise_result_no_code_issues,
                    score,
                    buildInformation,
                    points
                )
            } else {
                context.getString(
                    R.string.exercise_result_no_code_issues_without_points,
                    score,
                    buildInformation
                )
            }
        }

        private fun coerceIntToString(value: Int): String =
            if (value >= MAX_PROGRAMMING_RESULT_INTS) "$MAX_PROGRAMMING_RESULT_INTS+" else "$value"
    }
}
