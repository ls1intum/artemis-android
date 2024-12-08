package de.tum.informatics.www1.artemis.native_app.core.common.markdown

class PostArtemisMarkdownTransformer(val serverUrl: String, val courseId: Long) : ArtemisMarkdownTransformer() {
    override fun transformExerciseMarkdown(title: String, url: String): String {
        return "[$title](artemis:/$url)"
    }

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String = "[@$fullName](artemis://courses/$courseId/messages?username=$userName)"

    override fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String = "[#$channelName](artemis://courses/$courseId/messages?conversationId=$conversationId)"

    override fun transformLectureContentMarkdown(
        type: String,
        fileName: String,
        url: String
    ): String {
        return when (type) {
            "attachment" -> "[$fileName](artemis:/$url)"
            "lecture-unit" -> "[$fileName]($serverUrl/api/files/attachments/$url)" // TODO: fix authentication or redirect to lecture unit (https://github.com/ls1intum/artemis-android/issues/117)
            "slide" -> "[$fileName]($serverUrl/api/files/attachments/$url)" // TODO: fix authentication or redirect to lecture unit (https://github.com/ls1intum/artemis-android/issues/117)
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
