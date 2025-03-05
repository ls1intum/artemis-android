package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import androidx.annotation.DrawableRes
import de.tum.informatics.www1.artemis.native_app.core.common.R
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.MarkdownUrlUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ArtemisDeeplink
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks

const val TYPE_ICON_RESOURCE_PATH = "android.resource://de.tum.cit.aet.artemis/"
const val ATTACHMENTS_ENDPOINT = "/api/files/attachments/"

class PostArtemisMarkdownTransformer(val serverUrl: String, val courseId: Long) : ArtemisMarkdownTransformer() {

    private fun createFileTypeIconMarkdown(@DrawableRes drawableId: Int) =
        "![]($TYPE_ICON_RESOURCE_PATH$drawableId)"

    private fun createAttachmentsLink(serverUrl: String, fileName: String, filePath: String) =
        "[$fileName]($serverUrl$ATTACHMENTS_ENDPOINT$filePath)"

    override fun transformExerciseMarkdown(title: String, url: String, type: String): String {
        val namedLink = createInAppLinkWithTitle(title, url)
        val typeIcon =  when (type) {
            "text" -> R.drawable.font_link_icon
            "quiz" -> R.drawable.check_double_link_icon
            "lecture" -> R.drawable.chalkboard_teacher_link_icon
            "modeling" -> R.drawable.diagram_project_link_icon
            "file-upload" -> R.drawable.file_arrow_up_link_icon
            "programming" -> R.drawable.keyboard_link_icon
            else -> return namedLink
        }
        return "${createFileTypeIconMarkdown(typeIcon)}  $namedLink"
    }

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String {
        val link = CommunicationDeeplinks.ToOneToOneChatByUsername.inAppLink(courseId, userName)
        return "[@$fullName]($link)"
    }

    override fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String {
        val link = CommunicationDeeplinks.ToConversation.inAppLink(courseId, conversationId)
        return "${createFileTypeIconMarkdown(R.drawable.message_link_icon)}  [#$channelName]($link)"
    }

    override fun transformLectureContentMarkdown(
        type: String,
        fileName: String,
        url: String
    ): String {
        val fileIconImage = createFileTypeIconMarkdown(R.drawable.file_link_icon)
        val link = createAttachmentsLink(serverUrl, fileName, MarkdownUrlUtil.encodeUrl(url))

        return when (type) {
            "attachment" -> "$fileIconImage $link"
            "lecture-unit" -> "$fileIconImage $link"
            "slide" -> "!$link"
            else -> fileName
        }
    }

    public override fun transformFileUploadMessageMarkdown(
        isImage: Boolean,
        fileName: String,
        filePath: String
    ): String {
        return if (isImage) "![$fileName]($serverUrl$filePath)" else "[$fileName]($serverUrl$filePath)"
    }

    override fun transformFaqMarkdown(title: String, url: String): String {
        val namedLink = createInAppLinkWithTitle(title, url)
        return "${createFileTypeIconMarkdown(R.drawable.faq_link_icon)}  $namedLink"
    }

    private fun createInAppLinkWithTitle(title: String, url: String): String {
        return "[$title](${ArtemisDeeplink.IN_APP_HOST}$url)"
    }
}
