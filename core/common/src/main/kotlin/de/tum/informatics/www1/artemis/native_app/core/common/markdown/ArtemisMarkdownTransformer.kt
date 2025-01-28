package de.tum.informatics.www1.artemis.native_app.core.common.markdown

abstract class ArtemisMarkdownTransformer {

    /**
     * Empty markdown transformer.
     */
    companion object : ArtemisMarkdownTransformer() {
        override fun transformExerciseMarkdown(title: String, url: String, type: String): String =
            ""

        override fun transformUserMentionMarkdown(
            text: String,
            fullName: String,
            userName: String
        ): String = ""

        override fun transformChannelMentionMarkdown(
            channelName: String,
            conversationId: Long
        ): String = ""

        override fun transformLectureContentMarkdown(
            type: String,
            fileName: String,
            url: String
        ) : String = ""

        override fun transformFileUploadMessageMarkdown(
            isImage: Boolean,
            fileName: String,
            filePath: String,
        ) : String = ""
    }

    private val exerciseMarkdownPattern =
        "\\[(text|quiz|lecture|modeling|file-upload|programming)](.*)\\(((?:/|\\w|\\d)+)\\)\\[/\\1]".toRegex()
    private val userMarkdownPattern = "\\[user](.*?)\\((.*?)\\)\\[/user]".toRegex()
    private val channelMarkdownPattern = "\\[channel](.*?)\\((\\d+?)\\)\\[/channel]".toRegex()
    private val lectureContentMarkdownPattern = "\\[(attachment|lecture-unit|slide)](.*?)\\(([/\\w\\d\\-_\\.\\s]+)\\)\\[/\\1]".toRegex()
    private val fileUploadMessagePattern = "(\\!?)\\[(.*?)]\\((/api/files/[\\w\\d/\\-_.]+)\\)".toRegex()

    fun transformMarkdown(markdown: String): String {
        return exerciseMarkdownPattern.replace(markdown) { matchResult ->
            val type = matchResult.groups[1]?.value.orEmpty()
            val title = matchResult.groups[2]?.value.orEmpty()
            val url = matchResult.groups[3]?.value.orEmpty()
            transformExerciseMarkdown(title, url, type)
        }.let {
            userMarkdownPattern.replace(it) { matchResult ->
                val fullName = matchResult.groups[1]?.value.orEmpty()
                val userName = matchResult.groups[2]?.value.orEmpty()
                transformUserMentionMarkdown(
                    text = matchResult.groups[0]?.value.orEmpty(),
                    fullName = fullName,
                    userName = userName
                )
            }
        }.let {
            channelMarkdownPattern.replace(it) { matchResult ->
                val channelName = matchResult.groups[1]?.value.orEmpty()
                val conversationId = matchResult.groups[2]?.value.orEmpty().toLong()
                transformChannelMentionMarkdown(
                    channelName = channelName,
                    conversationId = conversationId
                )
            }
        }.let {
            lectureContentMarkdownPattern.replace(it) { matchResult ->
                val type = matchResult.groups[1]?.value.orEmpty()
                val fileName = matchResult.groups[2]?.value.orEmpty()
                val url = matchResult.groups[3]?.value.orEmpty()
                transformLectureContentMarkdown(
                    type = type,
                    fileName = fileName,
                    url = url
                )
            }
        }.let {
            fileUploadMessagePattern.replace(it) { matchResult ->
                // file uploads can be images or other files represented by a link:
                // image: ![fileName](url), file: [fileName](url)
                val isImage = matchResult.groups[1]?.value.orEmpty() == "!"
                val fileName = matchResult.groups[2]?.value.orEmpty()
                val filePath = matchResult.groups[3]?.value.orEmpty()
                transformFileUploadMessageMarkdown(
                    isImage = isImage,
                    fileName = fileName,
                    filePath = filePath
                )
            }
        }
    }

    protected abstract fun transformExerciseMarkdown(
        title: String,
        url: String,
        type: String
    ): String

    protected abstract fun transformUserMentionMarkdown(
        text: String,
        fullName: String,
        userName: String
    ): String

    protected abstract fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String

    protected abstract fun transformLectureContentMarkdown(
        type: String,
        fileName: String,
        url: String
    ): String

    protected abstract fun transformFileUploadMessageMarkdown(
        isImage: Boolean,
        fileName: String,
        filePath: String
    ): String
}
