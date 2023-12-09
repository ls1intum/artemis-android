package de.tum.informatics.www1.artemis.native_app.core.common.markdown

object PushNotificationArtemisMarkdownTransformer : ArtemisMarkdownTransformer() {

    override fun transformExerciseMarkdown(title: String, url: String): String = title

    override fun transformUserMentionMarkdown(text: String, fullName: String, userName: String): String = "@$fullName"

    override fun transformChannelMentionMarkdown(
        channelName: String,
        conversationId: Long
    ): String = "#$channelName"
}
