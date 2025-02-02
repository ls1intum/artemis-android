package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.ConversationIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

internal const val TEST_TAG_CONVERSATION_LIST = "conversation list"
internal const val TEST_TAG_HEADER_EXPAND_ICON = "expand icon"

internal const val SECTION_FAVORITES_KEY = "favorites"
internal const val SECTION_HIDDEN_KEY = "hidden"
internal const val SECTION_CHANNELS_KEY = "channels"
internal const val SECTION_GROUPS_KEY = "groups"
internal const val SECTION_EXERCISES_KEY = "exercises"
internal const val SECTION_EXAMS_KEY = "exams"
internal const val SECTION_LECTURES_KEY = "lectures"
internal const val SECTION_DIRECT_MESSAGES_KEY = "direct-messages"
internal const val SECTION_SAVED_POSTS_KEY = "saved-posts"
internal const val SECTION_RECENT_KEY = "recent"

internal const val KEY_SUFFIX_FAVORITES = "_f"
internal const val KEY_SUFFIX_CHANNELS = "_c"
internal const val KEY_SUFFIX_EXAMS = "_exa"
internal const val KEY_SUFFIX_EXERCISES = "_exe"
internal const val KEY_SUFFIX_LECTURES = "_l"
internal const val KEY_SUFFIX_GROUPS = "_g"
internal const val KEY_SUFFIX_PERSONAL = "_p"
internal const val KEY_SUFFIX_HIDDEN = "_h"
internal const val KEY_SUFFIX_SAVED_MESSAGES = "_s"
internal const val KEY_SUFFIX_RECENT = "_r"

internal fun tagForConversation(conversationId: Long, suffix: String) = "$conversationId$suffix"
internal fun tagForConversationOptions(tagForConversation: String) = "${tagForConversation}_options"

private sealed class ConversationSectionState(val isExpanded: Boolean) {
    data class Conversations<T : Conversation>(val conversations: ConversationCollections.ConversationCollection<T>)
        : ConversationSectionState(conversations.isExpanded)
    class SavedPosts(isExpanded: Boolean) : ConversationSectionState(isExpanded)
}

@Composable
internal fun ConversationList(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    conversationCollections: ConversationCollections,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (status: SavedPostStatus) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    trailingContent: LazyListScope.() -> Unit
) {
    val isSavedPostsExpanded by viewModel.isSavedPostsExpanded.collectAsState()

    ConversationList(
        modifier = modifier,
        isSavedPostsSectionExpanded = isSavedPostsExpanded,
        toggleFavoritesExpanded = viewModel::toggleFavoritesExpanded,
        toggleGeneralsExpanded = viewModel::toggleGeneralsExpanded,
        toggleExercisesExpanded = viewModel::toggleExercisesExpanded,
        toggleLecturesExpanded = viewModel::toggleLecturesExpanded,
        toggleExamsExpanded = viewModel::toggleExamsExpanded,
        toggleGroupChatsExpanded = viewModel::toggleGroupChatsExpanded,
        togglePersonalConversationsExpanded = viewModel::togglePersonalConversationsExpanded,
        toggleHiddenExpanded = viewModel::toggleHiddenExpanded,
        toggleSavedPostsExpanded = viewModel::toggleSavedPostsExpanded,
        toggleRecentExpanded = viewModel::toggleRecentExpanded,
        conversationCollections = conversationCollections,
        onNavigateToConversation = onNavigateToConversation,
        onNavigateToSavedPosts = onNavigateToSavedPosts,
        onToggleMarkAsFavourite = onToggleMarkAsFavourite,
        onToggleHidden = onToggleHidden,
        onToggleMuted = onToggleMuted,
        trailingContent = trailingContent
    )
}

@Composable
internal fun ConversationList(
    modifier: Modifier,
    isSavedPostsSectionExpanded: Boolean,
    toggleFavoritesExpanded: () -> Unit,
    toggleGeneralsExpanded: () -> Unit,
    toggleExercisesExpanded: () -> Unit,
    toggleLecturesExpanded: () -> Unit,
    toggleExamsExpanded: () -> Unit,
    toggleGroupChatsExpanded: () -> Unit,
    togglePersonalConversationsExpanded: () -> Unit,
    toggleHiddenExpanded: () -> Unit,
    toggleSavedPostsExpanded: () -> Unit,
    toggleRecentExpanded: () -> Unit,
    conversationCollections: ConversationCollections,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (status: SavedPostStatus) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    trailingContent: LazyListScope.() -> Unit
) {
    val listWithHeader: LazyListScope.(ConversationSectionState, String, String, Int, () -> Unit, @Composable () -> Unit) -> Unit =
        { items, key, suffix, textRes, onClick, icon ->
            conversationSectionHeader(
                key = key,
                text = textRes,
                isExpanded = items.isExpanded,
                onClick = onClick,
                icon = icon
            )

            conversationList(
                keySuffix = suffix,
                section = items,
                allowFavoriteIndicator = key != SECTION_FAVORITES_KEY,
                onNavigateToConversation = onNavigateToConversation,
                onNavigateToSavedPosts = onNavigateToSavedPosts,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden,
                onToggleMuted = onToggleMuted
            )
        }

    LazyColumn (
        modifier = modifier.testTag(TEST_TAG_CONVERSATION_LIST)
    ) {
        if (conversationCollections.favorites.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.favorites),
                SECTION_FAVORITES_KEY,
                KEY_SUFFIX_FAVORITES,
                R.string.conversation_overview_section_favorites,
                toggleFavoritesExpanded,
                { Icon(imageVector = Icons.Default.Favorite, contentDescription = null) }
            )
        }

        if (conversationCollections.recentChannels.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.recentChannels),
                SECTION_RECENT_KEY,
                KEY_SUFFIX_RECENT,
                R.string.conversation_overview_section_recent,
                toggleRecentExpanded,
                { Icon(imageVector = Icons.Default.AccessTimeFilled, contentDescription = null) }
            )
        }

        listWithHeader(
            ConversationSectionState.Conversations(conversationCollections.channels),
            SECTION_CHANNELS_KEY,
            KEY_SUFFIX_CHANNELS,
            R.string.conversation_overview_section_general_channels,
            toggleGeneralsExpanded
        ) { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null) }

        if (conversationCollections.exerciseChannels.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.exerciseChannels),
                SECTION_EXERCISES_KEY,
                KEY_SUFFIX_EXERCISES,
                R.string.conversation_overview_section_exercise_channels,
                toggleExercisesExpanded
            ) { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null) }
        }

        if (conversationCollections.lectureChannels.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.lectureChannels),
                SECTION_LECTURES_KEY,
                KEY_SUFFIX_LECTURES,
                R.string.conversation_overview_section_lecture_channels,
                toggleLecturesExpanded
            ) { Icon(imageVector = Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null) }
        }

        if (conversationCollections.examChannels.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.examChannels),
                SECTION_EXAMS_KEY,
                KEY_SUFFIX_EXAMS,
                R.string.conversation_overview_section_exam_channels,
                toggleExamsExpanded
            ) { Icon(imageVector = Icons.Default.School, contentDescription = null) }
        }

        if (conversationCollections.groupChats.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.groupChats),
                SECTION_GROUPS_KEY,
                KEY_SUFFIX_GROUPS,
                R.string.conversation_overview_section_groups,
                toggleGroupChatsExpanded
            ) { Icon(imageVector = Icons.Default.Forum, contentDescription = null) }
        }

        if (conversationCollections.directChats.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.directChats),
                SECTION_DIRECT_MESSAGES_KEY,
                KEY_SUFFIX_PERSONAL,
                R.string.conversation_overview_section_direct_messages,
                togglePersonalConversationsExpanded
            ) { Icon(imageVector = Icons.AutoMirrored.Filled.Message, contentDescription = null) }
        }

        if (conversationCollections.hidden.conversations.isNotEmpty()) {
            listWithHeader(
                ConversationSectionState.Conversations(conversationCollections.hidden),
                SECTION_HIDDEN_KEY,
                KEY_SUFFIX_HIDDEN,
                R.string.conversation_overview_section_hidden,
                toggleHiddenExpanded
            ) { Icon(imageVector = Icons.Default.Archive, contentDescription = null) }
        }

        listWithHeader(
            ConversationSectionState.SavedPosts(isExpanded = isSavedPostsSectionExpanded),
            SECTION_SAVED_POSTS_KEY,
            KEY_SUFFIX_SAVED_MESSAGES,
            R.string.conversation_overview_section_saved_posts,
            toggleSavedPostsExpanded
        ) { Icon(imageVector = Icons.Default.Bookmark, contentDescription = null) }

        trailingContent()
    }
}

private fun LazyListScope.conversationSectionHeader(
    key: String,
    @StringRes text: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    item(key = key) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(key)
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                icon()
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    text = stringResource(id = text),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            IconButton(
                modifier = Modifier.testTag(TEST_TAG_HEADER_EXPAND_ICON),
                onClick = { onClick() }
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun LazyListScope.conversationList(
    keySuffix: String,
    section: ConversationSectionState,
    allowFavoriteIndicator: Boolean,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (status: SavedPostStatus) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit
) {
    if (!section.isExpanded) return


    when(section) {
        is ConversationSectionState.SavedPosts -> {
            items(
                SavedPostStatus.entries
            ) {
                SavedPostsListItem(
                    modifier = Modifier.fillMaxWidth(),
                    status = it,
                    onClick = {
                        onNavigateToSavedPosts(it)
                    }
                )
            }
        }

        is ConversationSectionState.Conversations<*> -> {
            val conversations = section.conversations
            items(
                items = conversations.conversations,
                key = { tagForConversation(it.id, keySuffix) }
            ) { conversation ->
                val itemTag = tagForConversation(conversation.id, keySuffix)
                ConversationListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(itemTag),
                    itemBaseTag = itemTag,
                    conversation = conversation,
                    showPrefix = conversations.showPrefix,
                    allowFavoriteIndicator = allowFavoriteIndicator,
                    onNavigateToConversation = { onNavigateToConversation(conversation.id) },
                    onToggleMarkAsFavourite = {
                        onToggleMarkAsFavourite(
                            conversation.id,
                            !conversation.isFavorite
                        )
                    },
                    onToggleHidden = { onToggleHidden(conversation.id, !conversation.isHidden) },
                    onToggleMuted = { onToggleMuted(conversation.id, !conversation.isMuted) }
                )
            }
        }
    }
}

@Composable
private fun SavedPostsListItem(
    modifier: Modifier = Modifier,
    status: SavedPostStatus,
    onClick: () -> Unit,
) {
    ListItemBase(
        modifier = modifier,
        name = status.getUiText(),
        unreadMessagesCount = 0,
        grayedOut = false,
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = status.getIcon(),
                contentDescription = null
            )
        },
        otherTrailingContent = {}
    )
}

@Composable
private fun ListItemBase(
    modifier: Modifier = Modifier,
    name: String,
    unreadMessagesCount: Long,
    grayedOut: Boolean,
    onClick: () -> Unit,
    leadingContent: @Composable () -> Unit,
    otherTrailingContent: @Composable () -> Unit
) {
    val headlineColor = LocalContentColor.current.copy(alpha = if (grayedOut) 0.6f else 1f)

    Box(modifier = modifier) {
        ListItem(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = 24.dp)
                .height(48.dp),
            leadingContent = leadingContent,
            headlineContent = {
                Text(
                    text = name,
                    maxLines = 1,
                    color = headlineColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (unreadMessagesCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                    otherTrailingContent()
                }
            }
        )
    }
}

@Composable
private fun ConversationListItem(
    modifier: Modifier = Modifier,
    itemBaseTag: String,
    conversation: Conversation,
    showPrefix: Boolean,
    allowFavoriteIndicator: Boolean,
    onNavigateToConversation: () -> Unit,
    onToggleMarkAsFavourite: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleMuted: () -> Unit
) {
    val unreadMessagesCount = conversation.unreadMessagesCount ?: 0
    val displayName = getConversationTitle(conversation, showPrefix)

    ListItemBase(
        modifier = modifier,
        name = displayName,
        unreadMessagesCount = unreadMessagesCount,
        grayedOut = conversation.isMuted,
        onClick = onNavigateToConversation,
        leadingContent = {
            ConversationIcon(
                conversation = conversation,
                hasUnreadMessages = unreadMessagesCount > 0,
                allowFavoriteIndicator = allowFavoriteIndicator
            )
        },
        otherTrailingContent = {
            ConversationOptions(
                modifier = Modifier.testTag(tagForConversationOptions(itemBaseTag)),
                conversation = conversation,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden,
                onToggleMuted = onToggleMuted
            )
        }
    )
}

@Composable
private fun ConversationOptions(
    modifier: Modifier,
    conversation: Conversation,
    onToggleMarkAsFavourite: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleMuted: () -> Unit
) {
    var isContextDialogShown by remember { mutableStateOf(false) }
    val onDismissRequest = { isContextDialogShown = false }

    Box {
        IconButton(
            modifier = modifier,
            onClick = { isContextDialogShown = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = stringResource(R.string.conversation_overview_conversation_item_show_actions)
            )
        }

        ConversationListItemDropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            isContextDialogShown = isContextDialogShown,
            onDismissRequest = onDismissRequest,
            conversation = conversation,
            onToggleMarkAsFavourite = onToggleMarkAsFavourite,
            onToggleHidden = onToggleHidden,
            onToggleMuted = onToggleMuted
        )
    }
}

@Composable
private fun getConversationTitle(
    conversation: Conversation,
    showPrefix: Boolean = true
): String {
    return when (conversation) {
        is ChannelChat -> {
            val channelName = if (conversation.isArchived) {
                stringResource(
                    id = R.string.conversation_overview_archived_channel_name,
                    conversation.name
                )
            } else conversation.name

            if (showPrefix) {
                channelName
            } else {
                channelName.removeSectionPrefix()
            }
        }

        is GroupChat, is OneToOneChat -> {
            val humanReadableTitle = conversation.humanReadableName
            if (showPrefix) {
                humanReadableTitle
            } else {
                humanReadableTitle.removeSectionPrefix()
            }
        }

        else -> conversation.humanReadableName
    }
}

@Composable
private fun ConversationListItemDropdownMenu(
    modifier: Modifier,
    isContextDialogShown: Boolean,
    onDismissRequest: () -> Unit,
    conversation: Conversation,
    onToggleMarkAsFavourite: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleMuted: () -> Unit
) {
    DropdownMenu(
        expanded = isContextDialogShown,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = DpOffset(x = (-10).dp, y = 0.dp),
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = if (conversation.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = if (conversation.isFavorite) R.string.conversation_overview_conversation_item_unmark_as_favorite
                        else R.string.conversation_overview_conversation_item_mark_as_favorite
                    )
                )
            },
            onClick = {
                onToggleMarkAsFavourite()
                if (conversation.isHidden) {
                    onToggleHidden()
                }
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = if (conversation.isHidden) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = if (conversation.isHidden) R.string.conversation_overview_conversation_item_unmark_as_hidden
                        else R.string.conversation_overview_conversation_item_mark_as_hidden
                    )
                )
            },
            onClick = {
                onToggleHidden()
                if (conversation.isFavorite) {
                    onToggleMarkAsFavourite()
                }
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = if (conversation.isMuted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = if (conversation.isMuted) R.string.conversation_overview_conversation_item_unmark_as_muted
                        else R.string.conversation_overview_conversation_item_mark_as_muted
                    )
                )
            },
            onClick = {
                onToggleMuted()
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun UnreadMessages(modifier: Modifier = Modifier, unreadMessagesCount: Long) {
    if (unreadMessagesCount > 0) {
        Box(
            modifier = modifier
                .size(24.dp)
                .aspectRatio(1f)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unreadMessagesCount.toString(),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun String.removeSectionPrefix(): String {
    val prefixes = listOf("exercise-", "lecture-", "exam-")
    var result = this
    for (prefix in prefixes) {
        if (result.startsWith(prefix, ignoreCase = true)) {
            result = result.removePrefix(prefix)
            break
        }
    }
    return result.trim()
}