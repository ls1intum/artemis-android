package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val KEY_BUTTON_SHOW_COC = "KEY_BUTTON_SHOW_COC"

@Composable
fun ConversationOverviewBody(
    modifier: Modifier,
    courseId: Long,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (SavedPostStatus) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit,
    onRequestBrowseChannel: () -> Unit,
    canCreateChannel: Boolean
) {
    ConversationOverviewBody(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId) },
        onNavigateToConversation = onNavigateToConversation,
        onNavigateToSavedPosts = onNavigateToSavedPosts,
        onRequestCreatePersonalConversation = onRequestCreatePersonalConversation,
        onRequestAddChannel = onRequestAddChannel,
        onRequestBrowseChannel = onRequestBrowseChannel,
        canCreateChannel = canCreateChannel
    )
}

@Composable
fun ConversationOverviewBody(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (SavedPostStatus) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit,
    onRequestBrowseChannel: () -> Unit,
    canCreateChannel: Boolean
) {
    var showCodeOfConduct by rememberSaveable { mutableStateOf(false) }
    val conversationCollectionsDataState: DataState<ConversationCollections> by viewModel.conversations.collectAsState()

    val isConnected by viewModel.isConnected.collectAsState()

    val query by viewModel.query.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestReload()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BasicDataStateUi(
            modifier = modifier,
            dataState = conversationCollectionsDataState,
            loadingText = stringResource(id = R.string.conversation_overview_loading),
            failureText = stringResource(id = R.string.conversation_overview_loading_failed),
            retryButtonText = stringResource(id = R.string.conversation_overview_loading_try_again),
            onClickRetry = viewModel::requestReload
        ) { conversationCollection ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(modifier = Modifier.fillMaxWidth(), visible = !isConnected) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.WifiOff, contentDescription = null)

                            Text(
                                text = stringResource(id = R.string.conversation_overview_not_connected_banner),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                ConversationSearch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    query = query,
                    updateQuery = viewModel::onUpdateQuery
                )

                ConversationList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    conversationCollections = conversationCollection,
                    onNavigateToConversation = { conversationId ->
                        viewModel.setConversationMessagesRead(conversationId)
                        onNavigateToConversation(conversationId)
                    },
                    onNavigateToSavedPosts = onNavigateToSavedPosts,
                    onToggleMarkAsFavourite = viewModel::markConversationAsFavorite,
                    onToggleHidden = viewModel::markConversationAsHidden,
                    onToggleMuted = viewModel::markConversationAsMuted,
                    trailingContent = {
                        item { HorizontalDivider() }

                        item(key = KEY_BUTTON_SHOW_COC) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .padding(bottom = 24.dp)
                            ) {
                                TextButton(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = { showCodeOfConduct = true }
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null)

                                    Spacer(modifier = Modifier.size(8.dp))

                                    Text(text = stringResource(id = R.string.conversation_overview_button_show_code_of_conduct))
                                }
                            }
                        }
                    }
                )
            }
        }

        ConversationFabMenu(
            canCreateChannel = canCreateChannel,
            onCreateChat = onRequestCreatePersonalConversation,
            onBrowseChannels = onRequestBrowseChannel,
            onCreateChannel = onRequestAddChannel
        )
    }

    if (showCodeOfConduct) {
        ModalBottomSheet(
            contentWindowInsets = { WindowInsets.statusBars },
            onDismissRequest = { showCodeOfConduct = false }
        ) {
            CodeOfConductUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                courseId = viewModel.courseId
            )
        }
    }
}

@Composable
fun ConversationFabMenu(
    canCreateChannel: Boolean,
    onCreateChat: () -> Unit,
    onBrowseChannels: () -> Unit,
    onCreateChannel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateBottomPadding() + 8.dp,
                end = 16.dp
            )
            .imePadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.AddComment, contentDescription = "Add conversation")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 0.dp, y = (12).dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onCreateChat()
                    },
                    text = { Text(stringResource(id = R.string.create_chat_title)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onBrowseChannels()
                    },
                    text = { Text(stringResource(id = R.string.browse_channels_title)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Tag, contentDescription = null)
                    }
                )
                if (canCreateChannel) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onCreateChannel()
                        },
                        text = { Text(stringResource(id = R.string.create_channel_title)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AddComment, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun ConversationSearch(
    modifier: Modifier,
    query: String,
    updateQuery: (String) -> Unit
) {
    Box(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            BasicHintTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                hint = stringResource(id = R.string.conversation_overview_search_hint),
                value = query,
                onValueChange = updateQuery,
                maxLines = 1
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { updateQuery("") },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                        .padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

