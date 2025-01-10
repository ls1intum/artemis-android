package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.ui.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.ConversationDataStatusButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.isReplyEnabled
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.ConversationIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import io.github.fornewid.placeholder.material3.placeholder


@Composable
internal fun ConversationChatListScreen(
    modifier: Modifier,
    viewModel: ConversationViewModel,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
    onClickViewPost: (StandalonePostId) -> Unit
) {
    ConversationChatListScreen(
        modifier = modifier,
        courseId = viewModel.courseId,
        conversationId = viewModel.conversationId,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
        onClickViewPost = onClickViewPost
    )
}

@Composable
internal fun ConversationChatListScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    viewModel: ConversationViewModel,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
    onClickViewPost: (StandalonePostId) -> Unit
) {
    val query by viewModel.chatListUseCase.query.collectAsState()
    val conversationDataState by viewModel.latestUpdatedConversation.collectAsState()
    val conversationDataStatus by viewModel.conversationDataStatus.collectAsState()
    val clientId by viewModel.clientIdOrDefault.collectAsState()

    val chatListState: LazyListState = rememberLazyListState()

    val posts: LazyPagingItems<ChatListItem> =
        viewModel.chatListUseCase.postPagingData.collectAsLazyPagingItems()

    LaunchedEffect(courseId, conversationId) {
        chatListState.scrollToItem(0)
    }

    ConversationChatListScreen(
        modifier = modifier,
        courseId = courseId,
        conversationId = conversationId,
        clientId = clientId,
        conversationDataState = conversationDataState,
        conversationDataStatus = conversationDataStatus,
        query = query,
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
        onUpdateQuery = viewModel.chatListUseCase::updateQuery,
        onRequestSoftReload = viewModel::requestReload
    ) { padding ->
        val isReplyEnabled = isReplyEnabled(conversationDataState = conversationDataState)

        if (conversationDataState.isSuccess) {
            CompositionLocalProvider(LocalReplyAutoCompleteHintProvider provides viewModel) {
                MetisChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(top = padding.calculateTopPadding())
                        .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                    viewModel = viewModel,
                    onClickViewPost = onClickViewPost,
                    isReplyEnabled = isReplyEnabled,
                    state = chatListState,
                    posts = posts
                )
            }
        }
    }
}

@Composable
fun ConversationChatListScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    query: String,
    conversationDataStatus: DataStatus,
    conversationDataState: DataState<Conversation>,
    clientId: Long,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
    onUpdateQuery: (String) -> Unit,
    onRequestSoftReload: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var isSearchBarOpen by rememberSaveable(courseId, conversationId) { mutableStateOf(false) }

    val searchBarFocusRequester = remember { FocusRequester() }

    val closeSearch = {
        isSearchBarOpen = false
        onUpdateQuery("")
    }

    BackHandler(isSearchBarOpen, closeSearch)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchBarOpen) {
                        LaunchedEffect(Unit) {
                            searchBarFocusRequester.requestFocus()
                        }

                        BasicHintTextField(
                            modifier = Modifier.focusRequester(searchBarFocusRequester),
                            value = query,
                            onValueChange = onUpdateQuery,
                            hint = "",
                            maxLines = 1,
                            hintStyle = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        EmptyDataStateUi(
                            dataState = conversationDataState,
                            otherwise = {
                                Text("placeholder", Modifier.placeholder(true))
                            }
                        ) {
                            ConversationTitle(conversation = it, clientId = clientId)
                        }
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(
                            onClick = {
                                if (isSearchBarOpen) {
                                    closeSearch()
                                } else {
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (!isSearchBarOpen) {
                        if (BuildConfig.DEBUG){
                            ConversationDataStatusButton(
                                dataStatus = conversationDataStatus,
                                onRequestSoftReload = onRequestSoftReload
                            )
                        }

                        IconButton(onClick = { isSearchBarOpen = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                        }
                    }
                }
            )
        },
        content = content
    )
}


@Composable
private fun ConversationTitle(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    clientId: Long
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationIcon(
            conversation = conversation,
            clientId = clientId,
            showDialogOnOneToOneChatClick = true
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = conversation.humanReadableName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}