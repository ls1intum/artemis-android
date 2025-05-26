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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.common.selectionBorder
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationsOverviewSection
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common.ConversationIcon
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

internal fun tagForConversation(conversationId: Long, suffix: String) = "$conversationId$suffix"
internal fun tagForConversationOptions(tagForConversation: String) = "${tagForConversation}_options"

private sealed class ConversationSectionState(val isExpanded: Boolean) {
    data class Conversations<T : Conversation>(val conversations: ConversationCollections.ConversationCollection<T>)
        : ConversationSectionState(conversations.isExpanded)
}

@Composable
internal fun ConversationList(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    collapsingContentState: CollapsingContentState,
    conversationCollections: ConversationCollections,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: () -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    trailingContent: LazyListScope.() -> Unit,
    selectedConversationId: Long?
) {
    val scrollPosition by viewModel.scrollPosition.collectAsState()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = scrollPosition)

    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.saveScrollPosition(listState.firstVisibleItemIndex)
    }

    ConversationList(
        modifier = modifier,
        conversationCollections = conversationCollections,
        collapsingContentState = collapsingContentState,
        onToggleSection = viewModel::toggleSectionExpanded,
        onNavigateToConversation = onNavigateToConversation,
        onNavigateToSavedPosts = onNavigateToSavedPosts,
        onToggleMarkAsFavourite = onToggleMarkAsFavourite,
        onToggleHidden = onToggleHidden,
        onToggleMuted = onToggleMuted,
        trailingContent = trailingContent,
        selectedConversationId = selectedConversationId,
        listState = listState
    )
}

@Composable
internal fun ConversationList(
    modifier: Modifier,
    conversationCollections: ConversationCollections,
    collapsingContentState: CollapsingContentState,
    onToggleSection: (ConversationsOverviewSection) -> Unit,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: () -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    trailingContent: LazyListScope.() -> Unit,
    selectedConversationId: Long?,
    listState: LazyListState
) {
    LazyColumn(
        modifier = modifier
            .nestedScroll(collapsingContentState.nestedScrollConnection)
            .testTag(TEST_TAG_CONVERSATION_LIST),
        state = listState
    ) {
        for (conversationCollection in conversationCollections.collections) {
            val section = conversationCollection.section
            val conversations = conversationCollection.conversations

            if (conversations.isEmpty()) {
                continue
            }

            conversationSectionHeader(
                text = section.textRes,
                icon = {
                    Icon(imageVector = section.icon, contentDescription = null)
                },
                key = section.name,
                isExpanded = conversationCollection.isExpanded,
                conversationCount = conversations.size,
                unreadCount = conversations.sumOf { it.unreadMessagesCount ?: 0 },
                onClick = {
                    onToggleSection(section)
                },
            )

            conversationList(
                keySuffix = section.name,
                sectionState = ConversationSectionState.Conversations(conversationCollection),
                allowFavoriteIndicator = section != ConversationsOverviewSection.FAVOURITES,
                onNavigateToConversation = onNavigateToConversation,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden,
                onToggleMuted = onToggleMuted,
                selectedConversationId = selectedConversationId
            )
        }

        savedPostsHeaderRow(
            onClick = { onNavigateToSavedPosts() }
        )

        trailingContent()
    }
}

private fun LazyListScope.savedPostsHeaderRow(
    onClick: () -> Unit
) {
    conversationSectionHeader(
        key = SECTION_SAVED_POSTS_KEY,
        text = R.string.conversation_overview_section_saved_posts,
        isExpanded = null,
        unreadCount = 0,
        onClick = onClick,
        icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = null) }
    )
}

private fun LazyListScope.conversationSectionHeader(
    key: String,
    @StringRes text: Int,
    isExpanded: Boolean?,
    conversationCount: Int? = null,
    unreadCount: Long,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    item(key = key) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem()
                .testTag(key)
                .clickable { onClick() }
                .heightIn(min = 64.dp)      // If isExpanded is null, then the height is smaller because of the missing icon button
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
                    text = if (conversationCount != null) stringResource(id = text, conversationCount) else stringResource(id = text),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (isExpanded == null) {
                return@item
            }

            if (!isExpanded) UnreadMessages(unreadMessagesCount = unreadCount)

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
    sectionState: ConversationSectionState,
    allowFavoriteIndicator: Boolean,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    selectedConversationId: Long?
) {
    if (!sectionState.isExpanded) return

    when(sectionState) {
        is ConversationSectionState.Conversations<*> -> {
            val conversations = sectionState.conversations
            items(
                items = conversations.conversations,
                key = { tagForConversation(it.id, keySuffix) }
            ) { conversation ->
                val itemTag = tagForConversation(conversation.id, keySuffix)
                ConversationListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
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
                    onToggleMuted = { onToggleMuted(conversation.id, !conversation.isMuted) },
                    selected = conversation.id == selectedConversationId
                )
            }
        }
    }
}

@Composable
private fun ListItemBase(
    modifier: Modifier = Modifier,
    name: String,
    unreadMessagesCount: Long,
    grayedOut: Boolean,
    onClick: () -> Unit,
    leadingContent: @Composable () -> Unit,
    otherTrailingContent: @Composable () -> Unit,
    selected: Boolean = false
) {
    val headlineColor = LocalContentColor.current.copy(alpha = if (grayedOut) 0.6f else 1f)

    Box(modifier = modifier.selectionBorder(selected)) {
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
    onToggleMuted: () -> Unit,
    selected: Boolean = false
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
        },
        selected = selected
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
                style = MaterialTheme.typography.bodySmall,
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
