package de.tum.informatics.www1.artemis.native_app.core.data.service


private const val api = "api"

/**
 * Central location for all API endpoints.
 *
 * There were breaking changes introduced to the server API with Artemis 8.0.0 (see
 * https://github.com/ls1intum/Artemis/pull/10416). To run the app together with a version lower
 * than 8.0.0, remove the first level (module) of the paths (eg. "core", "communication", etc.).
 */
sealed class Api(
    vararg val path: String
) {

    // With 8.0.0 API changes:

//    data object Core: Api(api, "core") {
//        data object Public : Api(*Core.path, "public")
//        data object Courses : Api(*Core.path, "courses")
//    }
//
//    data object Communication: Api(api, "communication") {
//        data object Courses : Api(*Communication.path, "courses")
//        data object NotificationSettings : Api(*Communication.path, "notification-settings")
//        data object PushNotification : Api(*Communication.path, "push_notification")
//        data object SavedPosts : Api(*Communication.path, "saved-posts")
//    }
//
//    data object Lecture: Api(api, "lecture") {
//        data object Lectures : Api(*Lecture.path, "lectures")
//    }
//
//    data object Exercise: Api(api, "exercise") {
//        data object Exercises : Api(*Exercise.path, "exercises")
//    }
//
//    data object Text: Api(api, "text") {
//        data object TextExercises : Api(*Text.path, "text-exercises")
//    }
//
//    data object Modeling: Api(api, "modeling") {
//        data object ModelingExercises : Api(*Modeling.path, "modeling-exercises")
//    }
//
//    data object Programming: Api(api, "programming") {
//        data object ProgrammingExercises : Api(*Programming.path, "programming-exercises")
//    }
//
//    data object Quiz: Api(api, "quiz") {
//        data object QuizExercises : Api(*Quiz.path, "quiz-exercises")
//    }


    // Prior to 8.0.0 API changes:
    // Active for now to be compatible with the artemis-latest docker version used for e2e tests

    data object Core: Api(api) {
        data object Public : Api(*Core.path, "public")
        data object Courses : Api(*Core.path, "courses")
    }

    data object Communication: Api(api) {
        data object Courses : Api(*Communication.path, "courses")
        data object NotificationSettings : Api(*Communication.path, "notification-settings")
        data object PushNotification : Api(*Communication.path, "push_notification")
        data object SavedPosts : Api(*Communication.path, "saved-posts")
    }

    data object Lecture: Api(api) {
        data object Lectures : Api(*Lecture.path, "lectures")
    }

    data object Exercise: Api(api) {
        data object Exercises : Api(*Exercise.path, "exercises")
    }

    data object Text: Api(api) {
        data object TextExercises : Api(*Text.path, "text-exercises")
    }

    data object Modeling: Api(api) {
        data object ModelingExercises : Api(*Modeling.path, "modeling-exercises")
    }

    data object Programming: Api(api) {
        data object ProgrammingExercises : Api(*Programming.path, "programming-exercises")
    }

    data object Quiz: Api(api) {
        data object QuizExercises : Api(*Quiz.path, "quiz-exercises")
    }
}