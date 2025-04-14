package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletLandscape
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait
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
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
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
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = "Open sidebar"
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
        }
    )
}


@Composable
fun LayoutAwareTwoColumnLayout(
    modifier: Modifier = Modifier,
    isSidebarOpen: Boolean,
    onSidebarToggle: () -> Unit,
    optionalColumn: @Composable (Modifier) -> Unit,
    priorityColumn: @Composable (Modifier) -> Unit
) {
    val layout = getArtemisAppLayout()

    when {
        layout.isTabletPortrait -> {
            Box(modifier = modifier.fillMaxSize()) {
                priorityColumn(Modifier.fillMaxSize())

                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { onSidebarToggle() }
                            .zIndex(1f)
                    )
                }

                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
                    exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .zIndex(2f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        optionalColumn(Modifier.fillMaxSize())
                    }
                }
            }
        }

        layout.isTabletLandscape -> {
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
                    exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .zIndex(2f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        optionalColumn(Modifier.fillMaxSize())
                    }
                }

                VerticalDivider()

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    priorityColumn(Modifier.fillMaxSize())
                }
            }
        }

        else -> {
            // Phone
            Box(modifier = modifier) {
                priorityColumn(Modifier.fillMaxSize())
            }
        }
    }
}

