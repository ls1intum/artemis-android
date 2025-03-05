package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object FaqDeeplinks {

    object ToFaq : ArtemisDeeplink() {
        override val path = "courses/{courseId}/faq?faqId={faqId}"
        override val type = Type.IN_APP_AND_WEB

        fun markdownLink(courseId: Long, faqId: Long): String {
            return "/courses/$courseId/faq?faqId=$faqId"
        }
    }
}