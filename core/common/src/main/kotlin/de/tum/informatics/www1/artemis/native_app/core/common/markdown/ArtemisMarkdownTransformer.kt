package de.tum.informatics.www1.artemis.native_app.core.common.markdown

object ArtemisMarkdownTransformer {

    private val customMarkdownPattern = "\\[(text|quiz|lecture|modeling|file-upload|programing)](.*)\\(((?:/|\\w|\\d)+)\\)\\[/\\1]".toRegex()

    fun transformMarkdown(markdown: String, serverUrl: String): String {
        return customMarkdownPattern.replace(markdown) { matchResult ->
            val title = matchResult.groups[2]?.value.orEmpty()
            val url = matchResult.groups[3]?.value.orEmpty()
            "[$title]($serverUrl$url)"
        }
    }
}
