package de.tum.informatics.www1.artemis.native_app.core.data.service


private const val api = "api"

sealed class Api(
    vararg val path: String
) {

    data object Core: Api(api, "core") {
        data object Public : Api(*Core.path, "public")
        data object Courses : Api(*Core.path, "courses")
    }

    data object Communication: Api(api, "communication") {
        data object Courses : Api(*Communication.path, "courses")
        data object NotificationSettings : Api(*Communication.path, "notification-settings")
        data object PushNotification : Api(*Communication.path, "push_notification")
    }

    data object Lecture: Api(api, "lecture") {
        data object Lectures : Api(*Lecture.path, "lectures")
    }

    data object Exercise: Api(api, "exercise") {
        data object Exercises : Api(*Exercise.path, "exercises")
    }

    data object Text: Api(api, "text") {
        data object TextExercises : Api(*Text.path, "text-exercises")
    }

    data object Modeling: Api(api, "modeling") {
        data object ModelingExercises : Api(*Modeling.path, "modeling-exercises")
    }

    data object Programming: Api(api, "programming") {
        data object ProgrammingExercises : Api(*Programming.path, "programming-exercises")
    }

    data object Quiz: Api(api, "quiz") {
        data object QuizExercises : Api(*Quiz.path, "quiz-exercises")
    }
}