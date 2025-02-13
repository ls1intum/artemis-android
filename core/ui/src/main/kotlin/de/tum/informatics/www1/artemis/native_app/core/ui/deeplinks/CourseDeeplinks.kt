package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object CourseDeeplinks {

    object ToCourse : ArtemisDeeplink() {
        override val path = "courses/{courseId}"
        override val type = Type.IN_APP_AND_WEB
    }
}