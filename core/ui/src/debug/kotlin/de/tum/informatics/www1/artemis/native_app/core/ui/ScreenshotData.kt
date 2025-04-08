package de.tum.informatics.www1.artemis.native_app.core.ui

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData.Images.IMAGE_MARS
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData.Images.IMAGE_SATURN_5
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions

object ScreenshotData {

    object Images {
        const val IMAGE_MARS = "mars"
        const val IMAGE_SATURN_5 = "saturn5"
    }

    val course1 = Course(
        id = 1,
        title = "Advanced Aerospace Engineering \uD83D\uDE80",
        courseIconPath = IMAGE_SATURN_5,
        exercises = (0 until 5).map { TextExercise() },
        lectures = (0 until 3).map { Lecture() }
    )

    val course2 = Course(
        id = 2,
        title = "Manned space travel \uD83D\uDC68\u200D\uD83D\uDE80 \uD83D\uDC69\u200D\uD83D\uDE80",
        courseIconPath = IMAGE_MARS,
        exercises = (0 until 8).map { TextExercise() },
        lectures = (0 until 2).map { Lecture() }
    )

    val exercise1 = ModelingExercise(
        id = 1,
        title = "Designing a rocket engine",
        difficulty = Exercise.Difficulty.EASY,
    )

    val exercise2 = TextExercise(
        id = 2,
        title = "Sending rover to saturn",
        difficulty = Exercise.Difficulty.MEDIUM,
    )

    val exercise3 = ProgrammingExercise(
        id = 3,
        title = "Heat control on atmospheric reentry 🔥",
    )

    val exercises = listOf(
        exercise1,
        exercise2,
        exercise3
    )



    object Util {
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