package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

enum class ConversationsOverviewSection(
    val expandedByDefault: Boolean = true,
) {
    FAVOURITES(),
    CHANNELS(),
    EXERCISES(),
    LECTURES(),
    EXAMS(),
    GROUP_CHATS(),
    DIRECT_MESSAGES(),
    HIDDEN(expandedByDefault = false),
}