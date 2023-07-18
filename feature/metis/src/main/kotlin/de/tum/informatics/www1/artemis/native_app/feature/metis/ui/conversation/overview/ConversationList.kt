package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.ExtraChannelIcons
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.PrimaryChannelIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableTitle

internal const val TEST_TAG_CONVERSATION_LIST = "conversation list"

internal const val TEST_TAG_HEADER_EXPAND_ICON = "expand icon"

internal const val SECTION_FAVORITES_KEY = "favorites"
internal const val SECTION_HIDDEN_KEY = "hidden"
internal const val SECTION_CHANNELS_KEY = "channels"
internal const val SECTION_GROUPS_KEY = "groups"
internal const val SECTION_DIRECT_MESSAGES_KEY = "direct-messages"

internal const val KEY_SUFFIX_FAVORITES = "_f"
internal const val KEY_SUFFIX_CHANNELS = "_c"
internal const val KEY_SUFFIX_GROUPS = "_g"
internal const val KEY_SUFFIX_PERSONAL = "_p"
internal const val KEY_SUFFIX_HIDDEN = "_h"

internal fun tagForConversation(conversationId: Long, suffix: String) = "$conversationId$suffix"

@Composable
internal fun ConversationList(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    conversationCollections: ConversationCollections,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit
) {
    val listWithHeader: LazyListScope.(ConversationCollections.ConversationCollection<*>, String, String, Int, ConversationSectionHeaderAction, () -> Unit) -> Unit =
        { collection, key, suffix, textRes, action, toggleIsExpanded ->
            conversationSectionHeader(
                key = key,
                text = textRes,
                onClickAddAction = action,
                isExpanded = collection.isExpanded,
                toggleIsExpanded = toggleIsExpanded
            )

            conversationList(
                keySuffix = suffix,
                conversations = collection,
                onNavigateToConversation = onNavigateToConversation,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden
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
                viewModel::toggleFavoritesExpanded
            )
        }

        listWithHeader(
            conversationCollections.channels,
            SECTION_CHANNELS_KEY,
            KEY_SUFFIX_CHANNELS,
            R.string.conversation_overview_section_channels,
            OnClickAction(onRequestAddChannel),
            viewModel::toggleChannelsExpanded
        )

        listWithHeader(
            conversationCollections.groupChats,
            SECTION_GROUPS_KEY,
            KEY_SUFFIX_GROUPS,
            R.string.conversation_overview_section_groups,
            OnClickAction(onRequestCreatePersonalConversation),
            viewModel::toggleGroupChatsExpanded
        )

        listWithHeader(
            conversationCollections.directChats,
            SECTION_DIRECT_MESSAGES_KEY,
            KEY_SUFFIX_PERSONAL,
            R.string.conversation_overview_section_direct_messages,
            OnClickAction(onRequestCreatePersonalConversation),
            viewModel::togglePersonalConversationsExpanded
        )

        if (conversationCollections.hidden.conversations.isNotEmpty()) {
            listWithHeader(
                conversationCollections.hidden,
                SECTION_HIDDEN_KEY,
                KEY_SUFFIX_HIDDEN,
                R.string.conversation_overview_section_hidden,
                NoAction,
                viewModel::toggleHiddenExpanded
            )
        }
    }
}

private fun LazyListScope.conversationSectionHeader(
    key: String,
    @StringRes text: Int,
    isExpanded: Boolean,
    onClickAddAction: ConversationSectionHeaderAction,
    toggleIsExpanded: () -> Unit
) {
    item(key = key) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(key)
        ) {
            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.testTag(TEST_TAG_HEADER_EXPAND_ICON),
                    onClick = {
                        toggleIsExpanded()
                    }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    text = stringResource(id = text),
                    style = MaterialTheme.typography.titleSmall
                )

                if (onClickAddAction is OnClickAction) {
                    IconButton(
                        onClick = onClickAddAction.onClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                } else {
                    Box(modifier = Modifier.height(40.dp))
                }
            }

            Divider()
        }
    }
}

private fun <T : Conversation> LazyListScope.conversationList(
    keySuffix: String,
    conversations: ConversationCollections.ConversationCollection<T>,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
) {
    if (!conversations.isExpanded) return
    items(
        conversations.conversations,
        key = { tagForConversation(it.id, keySuffix) }) { conversation ->
        ConversationListItem(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tagForConversation(conversation.id, keySuffix)),
            conversation = conversation,
            onNavigateToConversation = { onNavigateToConversation(conversation.id) },
            onToggleMarkAsFavourite = {
                onToggleMarkAsFavourite(
                    conversation.id,
                    !conversation.isFavorite
                )
            },
            onToggleHidden = { onToggleHidden(conversation.id, !conversation.isHidden) },
            content = { contentModifier ->
                val unreadMessagesCount = conversation.unreadMessagesCount ?: 0

                when (conversation) {
                    is ChannelChat -> {
                        val channelName = if (conversation.isArchived) {
                            stringResource(
                                id = R.string.conversation_overview_archived_channel_name,
                                conversation.name
                            )
                        } else conversation.name

                        ListItem(
                            modifier = contentModifier,
                            leadingContent = {
                                PrimaryChannelIcon(channelChat = conversation)
                            },
                            headlineContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(text = channelName, maxLines = 1)

                                    ExtraChannelIcons(channelChat = conversation)
                                }
                            },
                            trailingContent = {
                                UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                            }
                        )
                    }

                    is GroupChat -> {
                        ListItem(
                            modifier = contentModifier,
                            headlineContent = { Text(conversation.humanReadableTitle) },
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
                            modifier = contentModifier,
                            headlineContent = { Text(conversation.humanReadableTitle) },
                            trailingContent = {
                                UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                            }
                        )
                    }
                }
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

@Composable
private fun ConversationListItem(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    onNavigateToConversation: () -> Unit,
    onToggleMarkAsFavourite: () -> Unit,
    onToggleHidden: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var isContextDialogShown by remember { mutableStateOf(false) }
    val onDismissRequest = { isContextDialogShown = false }

    Box(modifier = modifier) {
        content(
            Modifier.combinedClickable(
                onClick = onNavigateToConversation,
                onLongClick = { isContextDialogShown = true }
            )
        )

        DropdownMenu(
            expanded = isContextDialogShown,
            onDismissRequest = onDismissRequest
        ) {
            DropdownMenuItem(
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
        }
    }
}

private sealed interface ConversationSectionHeaderAction

private data class OnClickAction(val onClick: () -> Unit) : ConversationSectionHeaderAction

private object NoAction : ConversationSectionHeaderAction
