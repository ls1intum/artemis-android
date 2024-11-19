package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.ExtraChannelIcons
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.PrimaryChannelIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
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

internal const val KEY_SUFFIX_FAVORITES = "_f"
internal const val KEY_SUFFIX_CHANNELS = "_c"
internal const val KEY_SUFFIX_EXAMS = "_exa"
internal const val KEY_SUFFIX_EXERCISES = "_exe"
internal const val KEY_SUFFIX_LECTURES = "_l"
internal const val KEY_SUFFIX_GROUPS = "_g"
internal const val KEY_SUFFIX_PERSONAL = "_p"
internal const val KEY_SUFFIX_HIDDEN = "_h"

internal fun tagForConversation(conversationId: Long, suffix: String) = "$conversationId$suffix"
internal fun tagForConversationOptions(tagForConversation: String) = "${tagForConversation}_options"

@Composable
internal fun ConversationList(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    conversationCollections: ConversationCollections,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit,
    trailingContent: LazyListScope.() -> Unit
) {

    val listWithHeader: LazyListScope.(ConversationCollections.ConversationCollection<*>, String, String, Int, ConversationSectionHeaderAction, () -> Unit, @Composable () -> Unit) -> Unit =
        { collection, key, suffix, textRes, action, toggleIsExpanded, icon ->
            conversationSectionHeader(
                key = key,
                text = textRes,
                onClickAddAction = action,
                isExpanded = collection.isExpanded,
                toggleIsExpanded = toggleIsExpanded,
                icon = icon
            )

            conversationList(
                keySuffix = suffix,
                conversations = collection,
                showPrefix = collection.showPrefix,
                onNavigateToConversation = onNavigateToConversation,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden,
                onToggleMuted = onToggleMuted
            )
        }

    LazyColumn(modifier = modifier.testTag(TEST_TAG_CONVERSATION_LIST)) {
        if (conversationCollections.favorites.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.favorites,
                SECTION_FAVORITES_KEY,
                KEY_SUFFIX_FAVORITES,
                R.string.conversation_overview_section_favorites,
                NoAction,
                { viewModel.toggleFavoritesExpanded() },
                { Icon(imageVector = Icons.Default.Favorite, contentDescription = null) }
            )
        }

        listWithHeader(
            conversationCollections.channels,
            SECTION_CHANNELS_KEY,
            KEY_SUFFIX_CHANNELS,
            R.string.conversation_overview_section_general_channels,
            OnClickAction(onRequestAddChannel),
            viewModel::toggleGeneralsExpanded
        ) { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null) }

        if (conversationCollections.exerciseChannels.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.exerciseChannels,
                SECTION_EXERCISES_KEY,
                KEY_SUFFIX_EXERCISES,
                R.string.conversation_overview_section_exercise_channels,
                NoAction,
                viewModel::toggleExercisesExpanded
            ) { Icon(imageVector = Icons.Default.List, contentDescription = null) }
        }

        if (conversationCollections.lectureChannels.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.lectureChannels,
                SECTION_LECTURES_KEY,
                KEY_SUFFIX_LECTURES,
                R.string.conversation_overview_section_lecture_channels,
                NoAction,
                viewModel::toggleLecturesExpanded
            ) { Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null) }
        }

        if (conversationCollections.examChannels.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.examChannels,
                SECTION_EXAMS_KEY,
                KEY_SUFFIX_EXAMS,
                R.string.conversation_overview_section_exam_channels,
                NoAction,
                viewModel::toggleExamsExpanded
            ) { Icon(imageVector = Icons.Default.School, contentDescription = null) }
        }

        if (conversationCollections.groupChats.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.groupChats,
                SECTION_GROUPS_KEY,
                KEY_SUFFIX_GROUPS,
                R.string.conversation_overview_section_groups,
                OnClickAction(onRequestCreatePersonalConversation),
                viewModel::toggleGroupChatsExpanded
            ) { Icon(imageVector = Icons.Default.Forum, contentDescription = null) }
        }

        if (conversationCollections.directChats.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.directChats,
                SECTION_DIRECT_MESSAGES_KEY,
                KEY_SUFFIX_PERSONAL,
                R.string.conversation_overview_section_direct_messages,
                OnClickAction(onRequestCreatePersonalConversation),
                viewModel::togglePersonalConversationsExpanded
            ) { Icon(imageVector = Icons.Default.Message, contentDescription = null) }
        }

        if (conversationCollections.hidden.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.hidden,
                SECTION_HIDDEN_KEY,
                KEY_SUFFIX_HIDDEN,
                R.string.conversation_overview_section_hidden,
                NoAction,
                viewModel::toggleHiddenExpanded
            ) { Icon(imageVector = Icons.Default.NotInterested, contentDescription = null) }
        }

        trailingContent()
    }
}

private fun LazyListScope.conversationSectionHeader(
    key: String,
    @StringRes text: Int,
    isExpanded: Boolean,
    onClickAddAction: ConversationSectionHeaderAction,
    toggleIsExpanded: () -> Unit,
    icon: @Composable () -> Unit
) {
    item(key = key) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(key)
        ) {
            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { toggleIsExpanded() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                IconButton(
                    modifier = Modifier.testTag(TEST_TAG_HEADER_EXPAND_ICON),
                    onClick = { toggleIsExpanded() }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Divider()
        }
    }
}

private fun <T : Conversation> LazyListScope.conversationList(
    keySuffix: String,
    conversations: ConversationCollections.ConversationCollection<T>,
    showPrefix: Boolean,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onToggleMuted: (conversationId: Long, muted: Boolean) -> Unit,
) {
    if (!conversations.isExpanded) return
    items(
        conversations.conversations,
        key = { tagForConversation(it.id, keySuffix) }
    ) { conversation ->
        val itemTag = tagForConversation(conversation.id, keySuffix)
        ConversationListItem(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(itemTag),
            itemBaseTag = itemTag,
            conversation = conversation,
            showPrefix = showPrefix,
            onNavigateToConversation = { onNavigateToConversation(conversation.id) },
            onToggleMarkAsFavourite = {
                onToggleMarkAsFavourite(
                    conversation.id,
                    !conversation.isFavorite
                )
            },
            onToggleHidden = { onToggleHidden(conversation.id, !conversation.isHidden) },
            onToggleMuted = { onToggleMuted(conversation.id, !conversation.isMuted) },
        )
    }
}

@Composable
private fun ConversationListItem(
    modifier: Modifier = Modifier,
    itemBaseTag: String,
    conversation: Conversation,
    showPrefix: Boolean,
    onNavigateToConversation: () -> Unit,
    onToggleMarkAsFavourite: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleMuted: () -> Unit,
) {
    var isContextDialogShown by remember { mutableStateOf(false) }
    val onDismissRequest = { isContextDialogShown = false }

    val unreadMessagesCount = conversation.unreadMessagesCount ?: 0

    val headlineColor =
        LocalContentColor.current.copy(alpha = if (conversation.isMuted) 0.6f else 1f)

    val displayName = when (conversation) {
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

    Box(modifier = modifier) {
        when (conversation) {
            is ChannelChat -> {
                ListItem(
                    modifier = Modifier.clickable(onClick = onNavigateToConversation),
                    leadingContent = {
                        PrimaryChannelIcon(channelChat = conversation)
                    },
                    headlineContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = displayName, maxLines = 1, color = headlineColor)

                            ExtraChannelIcons(channelChat = conversation)
                        }
                    },
                    trailingContent = {
                        UnreadMessages(
                            modifier = Modifier.padding(end = 24.dp),
                            unreadMessagesCount = unreadMessagesCount
                        )
                    }
                )
            }

            is GroupChat -> {
                ListItem(
                    modifier = Modifier.clickable(onClick = onNavigateToConversation),
                    headlineContent = {
                        Text(
                            displayName,
                            color = headlineColor
                        )
                    },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Groups2, contentDescription = null)
                    },
                    trailingContent = {
                        UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                    }
                )
            }

            is OneToOneChat -> {
                ListItem(
                    modifier = Modifier.clickable(onClick = onNavigateToConversation),
                    headlineContent = {
                        Text(
                            displayName,
                            color = headlineColor
                        )
                    },
                    trailingContent = {
                        UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                    }
                )
            }
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .testTag(tagForConversationOptions(itemBaseTag)),
            onClick = { isContextDialogShown = true }
        ) {
            Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = null)
        }

        ConversationListItemDropdownMenu(
            modifier = Modifier.Companion.align(Alignment.TopEnd),
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
                    imageVector = if (conversation.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private sealed interface ConversationSectionHeaderAction

private data class OnClickAction(val onClick: () -> Unit) : ConversationSectionHeaderAction

private object NoAction : ConversationSectionHeaderAction

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
