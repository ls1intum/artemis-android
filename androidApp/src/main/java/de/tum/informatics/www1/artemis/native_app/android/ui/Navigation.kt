package de.tum.informatics.www1.artemis.native_app.android.ui

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Navigation {
    object Dest {
        const val HOME = "home"
        const val HOME_LOGIN = "home/login"
        const val HOME_REGISTER = "home/register"
        const val COURSE_OVERVIEW = "course_overview"
        const val COURSE_REGISTRATION = "course_overview/registration"
        const val COURSE_VIEW = "course/{courseId}"

        fun courseViewDestination(courseId: Int): String = "course/$courseId"
    }

    object Argument {
        const val COURSE_ID = "courseId"
    }

    object ArgDef {
        val COURSE_ID = navArgument(Argument.COURSE_ID) { type = NavType.IntType; nullable = false }
    }
}