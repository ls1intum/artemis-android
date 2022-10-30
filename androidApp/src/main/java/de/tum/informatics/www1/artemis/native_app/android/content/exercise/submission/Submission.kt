package de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Submission {

    abstract val id: Int?
    abstract val submitted: Boolean?
    abstract val submissionDate: Instant?
    abstract val type: SubmissionType?
    abstract val exampleSubmission: Boolean?
    abstract val submissionExerciseType: SubmissionExerciseType?
    abstract val durationInMinutes: Float?
    abstract val results: List<Result>?
    abstract val participation: Participation?

    enum class SubmissionType {
        MANUAL,
        TIMEOUT,
        INSTRUCTOR,
        EXTERNAL,
        TEST,
        ILLEGAL
    }

    // IMPORTANT NOTICE: The following strings have to be consistent with the ones defined in Submission.java
    enum class SubmissionExerciseType {
        @SerialName("programming")
        PROGRAMMING,

        @SerialName("modeling")
        MODELING,

        @SerialName("quiz")
        QUIZ,

        @SerialName("text")
        TEXT,

        @SerialName("file-upload")
        FILE_UPLOAD
    }
}