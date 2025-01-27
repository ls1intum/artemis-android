package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

internal enum class MarkdownStyle(val startTag: String, val endTag: String) {
    Bold("**", "**"),
    Italic("*", "*"),
    Underline("<ins>", "</ins>"),
    Strikethrough("~~", "~~"),
    InlineCode("`", "`"),
    CodeBlock("```", "```"),
    Blockquote("> ", ""),
    OrderedList("1. ", ""),
    UnorderedList("- ", ""),
    UserMention("@", ""),
    ChannelMention("#", ""),
    LectureMention("#", ""),
    ExerciseMention("#", "")
}