package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

private const val ConversationOverviewMaxWeight = 0.3f
private val ConversationOverviewMaxWidth = 600.dp

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
        getViewModel(key = "$courseId|$conversationId") { parametersOf(courseId, conversationId, threadPostId) }

    LaunchedEffect(threadPostId, viewModel) {
        viewModel.updateOpenedThread(threadPostId)
    }

    val widthSizeClass = getWindowSizeClass().widthSizeClass
    val metisContext = viewModel.metisContext

    when {
        widthSizeClass <= WindowWidthSizeClass.Compact -> {
            if (threadPostId != null) {
                ConversationThreadScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    standalonePostId = threadPostId,
                    metisContext = metisContext,
                    onNavigateUp = onCloseThread
                )
            } else {
                ConversationChatListScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    courseId = courseId,
                    conversationId = conversationId,
                    onNavigateBack = onCloseConversation,
                    onNavigateToSettings = onNavigateToSettings,
                    onClickViewPost = { clientPostId -> onOpenThread(StandalonePostId.ClientSideId(clientPostId)) }
                )
            }
        }

        else -> {
            val arrangement = Arrangement.spacedBy(8.dp)

            Row(
                modifier = modifier,
                horizontalArrangement = arrangement
            ) {
                val isOverviewVisible =
                    threadPostId == null || widthSizeClass >= WindowWidthSizeClass.Expanded
                AnimatedVisibility(
                    modifier = Modifier
                        .weight(ConversationOverviewMaxWeight)
                        .widthIn(max = ConversationOverviewMaxWidth)
                        .fillMaxHeight(),
                    visible = isOverviewVisible
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = arrangement
                    ) {
                        conversationsOverview(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        VerticalDivider()
                    }
                }

                val otherWeight = when {
                    isOverviewVisible && threadPostId != null -> 0.35f
                    isOverviewVisible && threadPostId == null -> 0.7f
                    else -> 0.5f
                }

                val otherModifier = Modifier
                    .weight(otherWeight)
                    .fillMaxHeight()

                ConversationChatListScreen(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    viewModel = viewModel,
                    courseId = courseId,
                    conversationId = conversationId,
                    onNavigateBack = onCloseConversation,
                    onNavigateToSettings = onNavigateToSettings,
                    onClickViewPost = { clientPostId -> onOpenThread(StandalonePostId.ClientSideId(clientPostId)) }
                )


                if (threadPostId != null) {
                    VerticalDivider()

                    ConversationThreadScreen(
                        modifier = otherModifier,
                        viewModel = viewModel,
                        standalonePostId = threadPostId,
                        metisContext = metisContext,
                        onNavigateUp = onCloseThread
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(DividerDefaults.color)
    )
}
