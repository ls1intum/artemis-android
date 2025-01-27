package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ArtemisDeeplink

object ExerciseViewDeeplinks {

    object ToExercise : ArtemisDeeplink() {
        override val path = "courses/{courseId}/exercises/{exerciseId}"
        override val type = Type.IN_APP_AND_WEB
    }

    object ToExerciseCourseAgnostic : ArtemisDeeplink() {
        override val path = "exercises/{exerciseId}"
        override val type = Type.ONLY_IN_APP
    }
}