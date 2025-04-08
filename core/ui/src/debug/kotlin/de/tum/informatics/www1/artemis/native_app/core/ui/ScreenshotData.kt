package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.UnknownSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData.Images.IMAGE_MARS
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData.Images.IMAGE_SATURN_5
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

object ScreenshotData {

    object Images {
        const val IMAGE_MARS = "mars"
        const val IMAGE_SATURN_5 = "saturn5"
    }

    val exercise1 = ModelingExercise(
        id = 1,
        title = "Designing a rocket engine",
        difficulty = Exercise.Difficulty.EASY,
        dueDate = Clock.System.now() + 1.days,
        maxPoints = 20f,
        studentParticipations = listOf(StudentParticipation.StudentParticipationImpl(
            testRun = false,
            submissions = listOf(
                UnknownSubmission(
                    submissionDate = Clock.System.now() - 1.days,
                )
            ),
            results = listOf(
                Result(
                    rated = true,
                    score = 100f,
                    completionDate = Clock.System.now() - 1.days,
                )
            ),
        ))
    )

    val exercise2 = TextExercise(
        id = 2,
        title = "Sending rover to saturn",
        difficulty = Exercise.Difficulty.MEDIUM,
        dueDate = Clock.System.now() + 3.days,
    )

    val exercise3 = ProgrammingExercise(
        id = 3,
        title = "Heat control on atmospheric reentry 🔥",
        dueDate = Clock.System.now() + 3.days,
    )

    val exercises = listOf(
        exercise1,
        exercise2,
        exercise3
    )

    val course1 = Course(
        id = 1,
        title = "Advanced Aerospace Engineering \uD83D\uDE80",
        courseIconPath = IMAGE_SATURN_5,
        exercises = listOf(exercise1),
    )

    val course2 = Course(
        id = 2,
        title = "Manned space travel \uD83D\uDC68\u200D\uD83D\uDE80 \uD83D\uDC69\u200D\uD83D\uDE80",
        courseIconPath = IMAGE_MARS,
    )

    val course1WithScores = CourseWithScore(
        course = course1,
        totalScores = CourseWithScore.TotalScores(
            100f,
            100f,
            studentScores = CourseWithScore.TotalScores.StudentScores(
                80f,
                0.8f,
                0.8f,
                0f
            )
        )
    )

    val course2WithScores = CourseWithScore(
        course = course2,
        totalScores = CourseWithScore.TotalScores(
            100f,
            100f,
            studentScores = CourseWithScore.TotalScores.StudentScores(
                91f,
                0.9f,
                0.9f,
                0f
            )
        )
    )



    object Util {
        @OptIn(ExperimentalCoilApi::class)
        @Composable
        fun configImagePreviewHandler(): AsyncImagePreviewHandler {
            val marsImage = ImageBitmap.imageResource(R.drawable.mars).asAndroidBitmap().asImage()
            val saturnImage = ImageBitmap.imageResource(R.drawable.saturn5).asAndroidBitmap().asImage()
            return AsyncImagePreviewHandler { request ->
                when (request.data) {
                    IMAGE_MARS -> marsImage
                    IMAGE_SATURN_5 -> saturnImage
                    else -> null
                }
            }
        }
        
        val emptyBoundExerciseActions = BoundExerciseActions(
            onClickStartTextExercise = {},
            onClickPracticeQuiz = {},
            onClickOpenQuiz = {},
            onClickStartQuiz = {},
            onClickOpenTextExercise = { _, _ -> },
            onClickViewResult = {},
            onClickViewQuizResults = {}
        )

        fun searchConfiguration(
            hint: String,
        ) = CourseSearchConfiguration.Search(
            query = "",
            hint = hint,
            onUpdateQuery = {}
        )
    }
}