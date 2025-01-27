package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object LectureDeeplinks {

    object ToLecture : ArtemisDeeplink() {
        override val path = "courses/{courseId}/lectures/{lectureId}"
        override val type = Type.IN_APP_AND_WEB

        fun markdownLink(courseId: Long, lectureId: Long): String {
            return "/courses/$courseId/lectures/$lectureId"
        }
    }

    object ToLectureCourseAgnostic : ArtemisDeeplink() {
        override val path = "lectures/{lectureId}"
        override val type = Type.ONLY_IN_APP
    }
}