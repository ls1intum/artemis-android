package de.tum.informatics.www1.artemis.native_app.core.common.markdown

import androidx.annotation.DrawableRes
import de.tum.informatics.www1.artemis.native_app.core.common.R

const val TYPE_ICON_RESOURCE_PATH = "android.resource://de.tum.cit.aet.artemis/"

class PostArtemisMarkdownTransformer(val serverUrl: String, val courseId: Long) : ArtemisMarkdownTransformer() {

    private fun createFileTypeIconMarkdown(@DrawableRes drawableId: Int) = "![]($TYPE_ICON_RESOURCE_PATH$drawableId)"

    override fun transformExerciseMarkdown(title: String, url: String, type: String): String {
        val typeIcon =  when (type) {
            "text" -> R.drawable.font_link_icon
            "quiz" -> R.drawable.check_double_link_icon
            "lecture" -> R.drawable.chalkboard_teacher_link_icon
            "modeling" -> R.drawable.diagram_project_link_icon
            "file-upload" -> R.drawable.file_arrow_up_link_icon
            "programming" -> R.drawable.keyboard_link_icon
            else -> return "[$title](artemis:/$url)"
        }
        return "${createFileTypeIconMarkdown(typeIcon)}  [$title](artemis:/$url)"
    }

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String = "[@$fullName](artemis://courses/$courseId/messages?username=$userName)"

    override fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String = "${createFileTypeIconMarkdown(R.drawable.message_link_icon)}  [#$channelName](artemis://courses/$courseId/messages?conversationId=$conversationId)"

    override fun transformLectureContentMarkdown(
        type: String,
        fileName: String,
        url: String
    ): String {
        val fileIconImage = createFileTypeIconMarkdown(R.drawable.file_link_icon)
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
