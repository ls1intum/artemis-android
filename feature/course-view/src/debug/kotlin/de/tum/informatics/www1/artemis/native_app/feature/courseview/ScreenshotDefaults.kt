package de.tum.informatics.www1.artemis.native_app.feature.courseview

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise

val ScreenshotCourse = Course(
    title = "Advanced Aerospace Engineering \uD83D\uDE80",
    exercises = listOf(
        ModelingExercise(
            id = 1,
            title = "Designing a rocket engine"
        ),
        TextExercise(
            id = 2,
            title = "Sending a rover to saturn \uD83E\uDE90"
        ),
        ProgrammingExercise(
            id = 3,
            title = "Heat control on atmospheric entry \uD83D\uDD25"
        )
    )
)