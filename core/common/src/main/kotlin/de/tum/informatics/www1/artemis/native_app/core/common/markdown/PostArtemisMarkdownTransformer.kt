package de.tum.informatics.www1.artemis.native_app.core.common.markdown

class PostArtemisMarkdownTransformer(val serverUrl: String) : ArtemisMarkdownTransformer() {
    override fun transformExerciseMarkdown(title: String, url: String): String {
        return "[$title]($serverUrl$url)"
    }

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String = text
}
