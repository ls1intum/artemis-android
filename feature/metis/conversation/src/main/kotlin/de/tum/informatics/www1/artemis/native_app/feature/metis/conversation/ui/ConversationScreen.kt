package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
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
    conversationsOverview: @Composable (Modifier) -> Unit
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
        optionalColumn = conversationsOverview,
    ) { innerModifier ->
        AnimatedContent(
            modifier = innerModifier,
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
                    onClickViewPost = onOpenThread
                )
            }
        }
    }
}


@Composable
fun LayoutAwareTwoColumnLayout(
    modifier: Modifier = Modifier,
    optionalColumnWeight: Float = 1f,
    priorityColumnWeight: Float = 2f,
    optionalColumn: @Composable (Modifier) -> Unit,
    priorityColumn: @Composable (Modifier) -> Unit
) {
    when(getArtemisAppLayout()) {
        ArtemisAppLayout.Phone -> {
            Box(modifier = modifier) {
                priorityColumn(Modifier.fillMaxSize())
            }
        }

        ArtemisAppLayout.Tablet -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(optionalColumnWeight)
                        .fillMaxHeight()
                ) {
                    optionalColumn(Modifier.fillMaxSize())
                }

                VerticalDivider()

                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .weight(priorityColumnWeight)
                        .fillMaxHeight()
                ) {
                    priorityColumn(Modifier.fillMaxSize())
                }
            }
        }
    }
}
