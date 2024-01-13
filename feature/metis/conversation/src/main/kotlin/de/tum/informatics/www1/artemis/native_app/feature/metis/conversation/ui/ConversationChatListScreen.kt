package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.isReplyEnabled
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
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

    val title by remember(conversationDataState) {
        derivedStateOf {
            conversationDataState.bind { it.humanReadableName }.orElse("Conversation")
        }
    }

    ConversationChatListScreen(
        modifier = modifier,
        courseId = courseId,
        conversationId = conversationId,
        isConversationLoaded = conversationDataState.isSuccess,
        query = query,
        conversationTitle = title,
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
        onUpdateQuery = viewModel.chatListUseCase::updateQuery
    ) { padding ->
        val isReplyEnabled = isReplyEnabled(conversationDataState = conversationDataState)

        if (conversationDataState.isSuccess) {
            CompositionLocalProvider(LocalReplyAutoCompleteHintProvider provides viewModel) {
                MetisChatList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    viewModel = viewModel,
                    listContentPadding = PaddingValues(),
                    onClickViewPost = onClickViewPost,
                    isReplyEnabled = isReplyEnabled
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
    conversationTitle: String,
    query: String,
    isConversationLoaded: Boolean,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
    onUpdateQuery: (String) -> Unit,
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
                        Text(
                            modifier = Modifier.placeholder(!isConversationLoaded),
                            text = conversationTitle,
                            maxLines = 1
                        )
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
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (!isSearchBarOpen) {
                        IconButton(onClick = { isSearchBarOpen = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        }
                    }
                }
            )
        },
        content = content
    )
}
