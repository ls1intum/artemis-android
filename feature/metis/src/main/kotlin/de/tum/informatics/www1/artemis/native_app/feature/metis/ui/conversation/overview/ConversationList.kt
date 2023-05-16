package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableTitle

private const val SECTION_HIDDEN_KEY = "hidden"
private const val SECTION_CHANNELS_KEY = "channels"
private const val SECTION_GROUPS_KEY = "groups"
private const val SECTION_DIRECT_MESSAGES_KEY = "direct-messages"

@Composable
internal fun ConversationList(
    modifier: Modifier,
    conversationCollection: ConversationCollection,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit
) {
    val defaultConversationList: LazyListScope.(List<Conversation>) -> Unit =
        { conversations: List<Conversation> ->
            conversationList(
                conversations = conversations,
                onNavigateToConversation = onNavigateToConversation,
                onToggleMarkAsFavourite = onToggleMarkAsFavourite,
                onToggleHidden = onToggleHidden
            )
        }

    LazyColumn(modifier = modifier) {
        if (conversationCollection.favorites.isNotEmpty()) {
            conversationSectionHeader(
                key = SECTION_HIDDEN_KEY,
                text = R.string.conversation_overview_section_favorites,
                onClickAddAction = NoAction
            )

            defaultConversationList(conversationCollection.favorites)
        }

        conversationSectionHeader(
            key = SECTION_CHANNELS_KEY,
            text = R.string.conversation_overview_section_channels,
            onClickAddAction = OnClickAction(onRequestAddChannel)
        )

        defaultConversationList(conversationCollection.channels)

        conversationSectionHeader(
            key = SECTION_GROUPS_KEY,
            text = R.string.conversation_overview_section_groups,
            onClickAddAction = OnClickAction(onRequestCreatePersonalConversation)
        )

        defaultConversationList(conversationCollection.groupChats)

        conversationSectionHeader(
            key = SECTION_DIRECT_MESSAGES_KEY,
            text = R.string.conversation_overview_section_direct_messages,
            onClickAddAction = OnClickAction(onRequestCreatePersonalConversation)
        )

        defaultConversationList(conversationCollection.directChats)

        if (conversationCollection.hidden.isNotEmpty()) {
            conversationSectionHeader(
                key = SECTION_HIDDEN_KEY,
                text = R.string.conversation_overview_section_hidden,
                onClickAddAction = NoAction
            )

            defaultConversationList(conversationCollection.hidden)
        }
    }
}

private fun LazyListScope.conversationSectionHeader(
    key: String,
    @StringRes text: Int,
    onClickAddAction: ConversationSectionHeaderAction
) {
    item(key = key) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

private fun LazyListScope.conversationList(
    conversations: List<Conversation>,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onToggleMarkAsFavourite: (conversationId: Long, favorite: Boolean) -> Unit,
    onToggleHidden: (conversationId: Long, hidden: Boolean) -> Unit,
) {
    items(conversations, key = Conversation::id) { conversation ->
        ConversationListItem(
            modifier = Modifier.fillMaxWidth(),
            conversation = conversation,
            onNavigateToConversation = { onNavigateToConversation(conversation.id) },
            onToggleMarkAsFavourite = {
                onToggleMarkAsFavourite(
                    conversation.id,
                    !conversation.isFavorite
                )
            },
            onToggleHidden = { onToggleHidden(conversation.id, !conversation.isHidden) },
            content = {
                val unreadMessagesCount = conversation.unreadMessagesCount ?: 0

                when (conversation) {
                    is ChannelChat -> {
                        ListItem(modifier = it,
                            headlineText = { Text(conversation.name) },
                            trailingContent = {
                                UnreadMessages(unreadMessagesCount = unreadMessagesCount)
                            }
                        )
                    }

                    is GroupChat -> {
                        ListItem(
                            modifier = it,
                            headlineText = { Text(conversation.humanReadableTitle) },
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
                            modifier = it,
                            headlineText = { Text(conversation.humanReadableTitle) },
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
