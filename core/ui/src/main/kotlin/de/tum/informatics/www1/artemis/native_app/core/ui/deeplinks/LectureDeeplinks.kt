package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object LectureDeeplinks {

    object ToLecture : ArtemisDeeplink() {
        override val path = "courses/{courseId}/lectures/{lectureId}?lectureUnitId={lectureUnitId}&attachmentId={attachmentId}&page={page}"
        override val type = Type.IN_APP_AND_WEB

        fun markdownLink(courseId: Long, lectureId: Long): String {
            return "/courses/$courseId/lectures/$lectureId"
        }
    }

    object ToLectureUnit : ArtemisDeeplink() {
        override val path = "courses/{courseId}/lectures/{lectureId}/lecture-units/{lectureUnitId}?page={page}"
        override val type = Type.IN_APP_AND_WEB
    }

    object ToLectureMediaReference : ArtemisDeeplink() {
        override val path = "courses/{courseId}/lecture-media?referenceType={referenceType}&reference={reference}"
        override val type = Type.ONLY_IN_APP
    }

    object ToLectureCourseAgnostic : ArtemisDeeplink() {
        override val path = "lectures/{lectureId}?lectureUnitId={lectureUnitId}&attachmentId={attachmentId}&page={page}"
        override val type = Type.ONLY_IN_APP
    }
}