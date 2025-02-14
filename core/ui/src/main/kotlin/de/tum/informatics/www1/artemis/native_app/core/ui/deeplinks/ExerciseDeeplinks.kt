package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object ExerciseDeeplinks {

    object ToExerciseOverview : ArtemisDeeplink() {
        override val path = "courses/{courseId}/exercises"
        override val type = Type.IN_APP_AND_WEB
    }

    object ToExercise : ArtemisDeeplink() {
        override val path = "courses/{courseId}/exercises/{exerciseId}"
        override val type = Type.IN_APP_AND_WEB

        fun markdownLink(courseId: Long, exerciseId: Long): String {
            return "/courses/$courseId/exercises/$exerciseId"
        }

        fun inAppLink(courseId: Long, exerciseId: Long): String {
            return "$IN_APP_HOST/courses/$courseId/exercises/$exerciseId"
        }
    }

    object ToExerciseCourseAgnostic : ArtemisDeeplink() {
        override val path = "exercises/{exerciseId}"
        override val type = Type.ONLY_IN_APP
    }

    object ToQuizParticipation : ArtemisDeeplink() {
        override val path = "quiz_participation/{courseId}/{exerciseId}"
        override val type = Type.ONLY_IN_APP

        fun inAppLink(courseId: Long, exerciseId: Long): String {
            return "$IN_APP_HOST/quiz_participation/$courseId/$exerciseId"
        }
    }
}