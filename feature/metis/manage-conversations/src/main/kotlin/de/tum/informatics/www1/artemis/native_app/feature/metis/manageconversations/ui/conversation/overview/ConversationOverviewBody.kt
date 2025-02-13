package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.endOfPagePadding
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val KEY_BUTTON_SHOW_COC = "KEY_BUTTON_SHOW_COC"

@Composable
fun ConversationOverviewBody(
    modifier: Modifier,
    courseId: Long,
    collapsingContentState: CollapsingContentState,
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
        collapsingContentState = collapsingContentState,
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
    collapsingContentState: CollapsingContentState,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToSavedPosts: (SavedPostStatus) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit,
    onRequestBrowseChannel: () -> Unit,
    canCreateChannel: Boolean
) {
    var showCodeOfConduct by rememberSaveable { mutableStateOf(false) }
    val conversationCollectionsDataState: DataState<ConversationCollections> by viewModel.conversations.collectAsState()
    val isDisplayingErrorDialog by viewModel.isDisplayingErrorDialog.collectAsState()

    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestReload()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (collapsingContentState.isSearchIconShown) {
                collapsingContentState.isSearchIconShown = false
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = Spacings.ScreenHorizontalSpacing)
    ) {
        BasicDataStateUi(
            modifier = modifier,
            dataState = conversationCollectionsDataState,
            loadingText = stringResource(id = R.string.conversation_overview_loading),
            failureText = stringResource(id = R.string.conversation_overview_loading_failed),
            retryButtonText = stringResource(id = R.string.conversation_overview_loading_try_again),
            enablePullToRefresh = false,
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

                ConversationList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    collapsingContentState = collapsingContentState,
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
                        item(key = KEY_BUTTON_SHOW_COC) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .pagePadding()
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

        ConversationFabWithDropdownMenu(
            canCreateChannel = canCreateChannel,
            onCreateChat = onRequestCreatePersonalConversation,
            onBrowseChannels = onRequestBrowseChannel,
            onCreateChannel = onRequestAddChannel,
            onMarkAllAsRead = viewModel::markAllConversationsAsRead
        )
    }

    if (showCodeOfConduct) {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            contentWindowInsets = { WindowInsets.statusBars },
            onDismissRequest = { showCodeOfConduct = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            CodeOfConductUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .pagePadding(),
                courseId = viewModel.courseId
            )
        }
    }

    if (isDisplayingErrorDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.mark_all_messages_as_read_failed_title),
            text = stringResource(id = R.string.mark_all_messages_as_read_failed_message),
            confirmButtonText = stringResource(id = R.string.mark_all_messages_as_read_failed_positive),
            dismissButtonText = null,
            onPressPositiveButton = { viewModel.dismissErrorDialog() },
            onDismissRequest = { viewModel.dismissErrorDialog() }
        )
    }
}

@Composable
fun ConversationFabWithDropdownMenu(
    modifier: Modifier = Modifier,
    canCreateChannel: Boolean,
    onCreateChat: () -> Unit,
    onBrowseChannels: () -> Unit,
    onCreateChannel: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .endOfPagePadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            if (showDropdownMenu) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            FloatingActionButton(
                onClick = { showDropdownMenu = !showDropdownMenu },
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "Add conversation"
                )
            }

            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false },
            ) {
                DropdownMenuItem(
                    onClick = {
                        showDropdownMenu = false
                        onCreateChat()
                    },
                    text = { Text(stringResource(id = R.string.create_chat_title)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        showDropdownMenu = false
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
                            showDropdownMenu = false
                            onCreateChannel()
                        },
                        text = { Text(stringResource(id = R.string.create_channel_title)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AddComment, contentDescription = null)
                        }
                    )
                }
                DropdownMenuItem(
                    onClick = {
                        showDropdownMenu = false
                        onMarkAllAsRead()
                    },
                    text = { Text(stringResource(id = R.string.mark_all_messages_as_read)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Checklist, contentDescription = null)
                    }
                )
            }
        }
    }
}