package de.tum.informatics.www1.artemis.native_app.core.data.service


private const val api = "api"

/**
 * Central location for all API endpoints.
 *
 * There were breaking changes introduced to the server API with Artemis 8.0.0 (see
 * https://github.com/ls1intum/Artemis/pull/10416). To run the app together with a version lower
 * than 8.0.0, remove the first level (module) of the paths (eg. "core", "communication", etc.).
 * (See the git history of this file for the previous version.)
 *
 * Artemis 9.3 moved several resources out of the "core" and "communication" modules into modules of
 * their own: courses answer under "course", the account and passkey endpoints under "account",
 * everything notification-related under "notification", the admin endpoints under "admin" (user
 * administration under "account/admin"), and starting a quiz batch under "quiz/quiz-batches". The
 * server still serves the old spellings as deprecated aliases, but stops doing so on
 * 30 September 2026.
 */
sealed class Api(
    vararg val path: String
) {

    fun getJointPath(separator: String = "/"): String = path.joinToString(separator)

    // With 8.0.0 API changes:

    data object Account: Api(api, "account") {
        data object Admin : Api(*Account.path, "admin")
        data object Passkeys : Api(*Account.path, "passkeys")
    }

    data object Admin: Api(api, "admin")

    data object Core: Api(api, "core") {
        data object Public : Api(*Core.path, "public")
        data object Files : Api(*Core.path, "files")
    }

    data object Course: Api(api, "course") {
        data object Courses : Api(*Course.path, "courses")
    }

    data object Communication: Api(api, "communication") {
        data object Courses : Api(*Communication.path, "courses")
        data object SavedPosts : Api(*Communication.path, "saved-posts")

        /** To be used as a appended path segment after Communication.Courses */
        const val standalonePostSegment = "messages"
        /** To be used as a appended path segment after Communication.Courses */
        const val answerPostSegment = "answer-messages"
    }

    data object Notification: Api(api, "notification") {
        data object Courses : Api(*Notification.path, "courses")
        data object PushNotification : Api(*Notification.path, "push_notification")
    }

    data object Lecture: Api(api, "lecture") {
        data object Lectures : Api(*Lecture.path, "lectures")
    }

    data object Exercise: Api(api, "exercise") {
        data object Exercises : Api(*Exercise.path, "exercises")
    }

    data object Text: Api(api, "text") {
        data object Participations : Api(*Text.path, "participations")
        data object TextExercises : Api(*Text.path, "text-exercises")
    }

    data object Modeling: Api(api, "modeling") {
        data object ModelingExercises : Api(*Modeling.path, "modeling-exercises")
    }

    data object Programming: Api(api, "programming") {
        data object ProgrammingExercises : Api(*Programming.path, "programming-exercises")
    }

    data object Quiz: Api(api, "quiz") {
        data object QuizBatches : Api(*Quiz.path, "quiz-batches")
        data object QuizExercises : Api(*Quiz.path, "quiz-exercises")
    }
}
