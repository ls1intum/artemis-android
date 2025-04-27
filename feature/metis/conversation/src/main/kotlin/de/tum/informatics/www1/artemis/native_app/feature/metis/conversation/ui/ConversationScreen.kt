package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Displays a conversation. Uses [ConversationChatListScreen] and [ConversationThreadScreen] to show the conversations.
 * @param conversationId the conversation that should be displayed
 * @param threadPostId the post that should be opened as a thread.
 * @param conversationsOverview if the layout has a lot of horizontal space, we want to show the conversation overview
 */
@Composable
fun ConversationScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    threadPostId: StandalonePostId?,
    onOpenThread: (postId: StandalonePostId) -> Unit,
    onCloseThread: () -> Unit,
    onCloseConversation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    conversationsOverview: @Composable (Modifier) -> Unit,
    showEmptyMessage: Boolean = false,
    isSidebarOpen: Boolean = false,
    onSidebarToggle: () -> Unit,
    title: String?
) {
    val viewModel: ConversationViewModel =
        koinViewModel(
            key = "$courseId|$conversationId",
        ) { parametersOf(courseId, conversationId, threadPostId) }

    LaunchedEffect(threadPostId, viewModel) {
        viewModel.updateOpenedThread(threadPostId)
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.chatListUseCase.resetLastAlreadyReadPostId() }
    }

    val showThread by remember(threadPostId) {
        mutableStateOf(threadPostId != null)
    }

    LayoutAwareTwoColumnLayout(
        modifier = modifier,
        isSidebarOpen = isSidebarOpen,
        onSidebarToggle = onSidebarToggle,
        optionalColumn = { sidebarMod ->
            conversationsOverview(sidebarMod)
        },
        priorityColumn = { contentMod ->
            if(showEmptyMessage) {
                IconButton(onClick = onSidebarToggle) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open sidebar"
                    )
                }
                Box(
                    modifier = contentMod.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please select a conversation from the sidebar.")
                }
            } else {
                IconButton(onClick = onSidebarToggle) {
                    Icon(
                        imageVector = Icons.Filled.Menu,                        contentDescription = "Open sidebar"
                    )
                }
                AnimatedContent(
                    modifier = contentMod,
                    targetState = showThread,
                    transitionSpec = {
                        if (targetState) {
                            DefaultTransition.navigateForward
                        } else {
                            DefaultTransition.navigateBack
                        }.using(
                            SizeTransform(clip = false)
                        )
                    },
                    label = "ConversationScreen chatList thread navigation animation"
                ) { _showThread ->
                    if (_showThread) {
                        ConversationThreadScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            onNavigateUp = onCloseThread
                        )
                    } else {
                        ConversationChatListScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            onNavigateBack = onCloseConversation,
                            onNavigateToSettings = onNavigateToSettings,
                            onClickViewPost = onOpenThread,
                            onSidebarToggle = onSidebarToggle
                        )
                    }
                }
            }
        },
        title = title
    )
}


