package de.tum.informatics.www1.artemis.native_app.core.common.markdown

import de.tum.informatics.www1.artemis.native_app.core.common.R

class PostArtemisMarkdownTransformer(val serverUrl: String, val courseId: Long) : ArtemisMarkdownTransformer() {

    private val resourcePath = "android.resource://de.tum.cit.aet.artemis/"

    override fun transformExerciseMarkdown(title: String, url: String, type: String): String {
        val typeIcon =  when (type) {
            "text" -> R.drawable.font_link_preview
            "quiz" -> R.drawable.check_double_link_preview
            "lecture" -> R.drawable.chalkboard_teacher_link_preview
            "modeling" -> R.drawable.diagram_project_link_preview
            "file-upload" -> R.drawable.file_arrow_up_link_preview
            "programming" -> R.drawable.keyboard_link_preview
            else -> return "[$title](artemis:/$url)"
        }
        return "![]($resourcePath$typeIcon)  [$title](artemis:/$url)"
    }

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String = "[@$fullName](artemis://courses/$courseId/messages?username=$userName)"

    override fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String = "![]($resourcePath${R.drawable.message_link_preview})  [#$channelName](artemis://courses/$courseId/messages?conversationId=$conversationId)"

    override fun transformLectureContentMarkdown(
        type: String,
        fileName: String,
        url: String
    ): String {
        val fileIconImage = "![]($resourcePath${R.drawable.file_link_preview})"
        return when (type) {
            "attachment" -> "$fileIconImage [$fileName](artemis:/$url)"
            "lecture-unit" -> "$fileIconImage [$fileName]($serverUrl/api/files/attachments/$url)" // TODO: fix authentication or redirect to lecture unit (https://github.com/ls1intum/artemis-android/issues/117)
            "slide" -> "$fileIconImage [$fileName]($serverUrl/api/files/attachments/$url)" // TODO: fix authentication or redirect to lecture unit (https://github.com/ls1intum/artemis-android/issues/117)
            else -> fileName
        }
    }

    override fun transformFileUploadMessageMarkdown(
        isImage: Boolean,
        fileName: String,
        filePath: String
    ): String {
        // TODO: fix authentication or redirect for all non-image uploads (https://github.com/ls1intum/artemis-android/issues/117)
        return if (isImage) "![$fileName]($serverUrl$filePath)" else "[$fileName]($serverUrl$filePath)"
    }
}
