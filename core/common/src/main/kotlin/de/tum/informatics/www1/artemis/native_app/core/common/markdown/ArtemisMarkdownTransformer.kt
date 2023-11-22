package de.tum.informatics.www1.artemis.native_app.core.common.markdown

abstract class ArtemisMarkdownTransformer {

    /**
     * Empty markdown transformer.
     */
    companion object : ArtemisMarkdownTransformer() {
        override fun transformExerciseMarkdown(title: String, url: String): String = ""

        override fun transformUserMentionMarkdown(
            text: String,
            fullName: String,
            userName: String
        ): String = ""
    }

    private val exerciseMarkdownPattern =
        "\\[(text|quiz|lecture|modeling|file-upload|programing)](.*)\\(((?:/|\\w|\\d)+)\\)\\[/\\1]".toRegex()
    private val userMarkdownPattern = "\\[user](.*?)\\((.*?)\\)\\[/user]".toRegex()

    fun transformMarkdown(markdown: String): String {
        return exerciseMarkdownPattern.replace(markdown) { matchResult ->
            val title = matchResult.groups[2]?.value.orEmpty()
            val url = matchResult.groups[3]?.value.orEmpty()
            transformExerciseMarkdown(title, url)
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
        }
    }

    protected abstract fun transformExerciseMarkdown(title: String, url: String): String

    protected abstract fun transformUserMentionMarkdown(
        text: String,
        fullName: String,
        userName: String
    ): String
}
