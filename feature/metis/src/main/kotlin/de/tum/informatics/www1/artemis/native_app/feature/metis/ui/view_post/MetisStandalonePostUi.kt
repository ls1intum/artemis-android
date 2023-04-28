package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.*
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.MarkdownTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.AnswerPostItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleStandalonePostDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Displays a single post with its replies.
 * @param viewType is currently unused, but should be used to scroll to the requested content in the future.
 */
@Composable
internal fun MetisStandalonePostUi(
    modifier: Modifier,
    viewModel: MetisStandalonePostViewModel,
    @Suppress("UNUSED_PARAMETER") viewType: ViewType
) {
    val postDataState: DataState<Post> by viewModel.post.collectAsState()
    val clientId: Long = viewModel.clientId.collectAsState().value.orElse(0L)

    postDataState.bind { it.serverPostId }.orNull()?.let { serverSidePostId ->
        ReportVisibleMetisContext(
            remember(
                viewModel.metisContext,
                serverSidePostId
            ) {
                VisibleStandalonePostDetails(viewModel.metisContext, serverSidePostId)
            }
        )
    }


    var metisFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    ProvideEmojis {
        BoxWithConstraints(modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                BasicDataStateUi(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    dataState = postDataState,
                    loadingText = stringResource(id = R.string.standalone_post_loading),
                    failureText = stringResource(id = R.string.standalone_post_failure),
                    retryButtonText = stringResource(id = R.string.standalone_post_try_again),
                    onClickRetry = viewModel::requestReload
                ) { post ->
                    PostAndRepliesList(
                        modifier = Modifier.fillMaxSize(),
                        post = post,
                        viewModel = viewModel,
                        clientId = clientId,
                        setMetisFailure = { metisFailure = it }
                    )
                }

                var replyContent: String by rememberSaveable {
                    mutableStateOf("")
                }

                val scope = rememberCoroutineScope()
                val replyState: ReplyState by remember {
                    val state = mutableStateOf<ReplyState>(ReplyState.HasSentReply)

                    state.value = createCanCreateReplyState(
                        scope,
                        viewModel,
                        updateReplyState = { state.value = it },
                        updateFailureState = { metisFailure = it },
                        clearReplyContent = {
                            replyContent = ""
                        }
                    )

                    state
                }

                AnimatedVisibility(visible = postDataState.isSuccess) {
                    ReplyUi(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = this@BoxWithConstraints.maxHeight * 0.6f),
                        replyState = replyState,
                        replyContent = replyContent,
                        updateReplyContent = { replyContent = it }
                    )
                }
            }
        }
    }

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
    }
}

@Composable
private fun PostAndRepliesList(
    modifier: Modifier,
    post: Post,
    viewModel: MetisStandalonePostViewModel,
    clientId: Long,
    setMetisFailure: (MetisModificationFailure?) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val asAffectedPost = post.let {
                MetisModificationService.AffectedPost.Standalone(
                    postId = it.serverPostId
                )
            }

            PostItem(
                modifier = Modifier.padding(top = 8.dp),
                post = post,
                postItemViewType = PostItemViewType.StandaloneView,
                onReactWithEmoji = { emojiId ->
                    viewModel.createReaction(
                        emojiId = emojiId,
                        post = asAffectedPost,
                        response = setMetisFailure
                    )
                },
                onClickOnPresentReaction = { emojiId ->
                    viewModel.onClickReaction(
                        emojiId = emojiId,
                        post = asAffectedPost,
                        presentReactions = post.reactions,
                        response = setMetisFailure
                    )
                },
                clientId = clientId
            )
        }

        item {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(id = R.string.standalone_screen_replies_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        items(post.orderedAnswerPostings, key = { it.postId }) { answerPost ->
            AnswerPostItem(
                modifier = Modifier.fillMaxWidth(),
                answerPost = answerPost,
                onReactWithEmoji = { emojiId ->
                    viewModel.createReactionForAnswer(
                        emojiId = emojiId,
                        clientSidePostId = answerPost.postId,
                        onResponse = setMetisFailure
                    )
                },
                onClickOnPresentReaction = { emojiId ->
                    viewModel.onClickReactionOfAnswer(
                        emojiId = emojiId,
                        clientSidePostId = answerPost.postId,
                        presentReactions = answerPost.reactions,
                        onResponse = setMetisFailure
                    )
                },
                answerItem = PostItemViewType.AnswerItem(
                    // TODO: Implement this logic once logic has been moved to server
                    canEdit = false,
                    canDelete = false,
                    onClickEdit = {},
                    onClickDelete = {}
                ),
                clientId = clientId
            )
        }
    }
}

@Composable
private fun ReplyUi(
    modifier: Modifier,
    replyContent: String,
    updateReplyContent: (String) -> Unit,
    replyState: ReplyState
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(5)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                targetState = replyState
            ) { targetReplyState ->
                when (targetReplyState) {
                    is ReplyState.CanCreate -> {
                        CreateReplyUi(
                            modifier = Modifier.fillMaxWidth(),
                            onReply = targetReplyState.onCreateReply,
                            replyContent = replyContent,
                            updateReplyContent = updateReplyContent
                        )
                    }

                    ReplyState.HasSentReply -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    is ReplyState.IsSendingReply -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(30.dp)
                            )

                            Text(
                                text = stringResource(id = R.string.create_answer_sending_reply),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = targetReplyState.onCancelSendReply) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateReplyUi(
    modifier: Modifier,
    replyContent: String,
    updateReplyContent: (String) -> Unit,
    onReply: (String) -> Unit
) {
    var displayTextField: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (displayTextField) {
            val focusRequest = remember { FocusRequester() }

            MarkdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                text = replyContent,
                onTextChanged = updateReplyContent,
                focusRequester = focusRequest,
                onFocusLost = {
                    if (displayTextField && replyContent.isEmpty()) {
                        displayTextField = false
                    }
                },
                sendButton = {
                    IconButton(
                        onClick = { onReply(replyContent) },
                        enabled = replyContent.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    }
                }
            )

            LaunchedEffect(key1 = displayTextField) {
                focusRequest.requestFocus()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { displayTextField = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.create_answer_click_to_write),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )
            }
        }

    }
}

/**
 * Factory to not duplicate can create reply state code.
 * @param updateReplyState set the UI reply state state
 * @param updateFailureState set failure state to show dialog if creating reply failed.
 * @param clearReplyContent set the reply content back to an empty string
 */
private fun createCanCreateReplyState(
    coroutineScope: CoroutineScope,
    viewModel: MetisStandalonePostViewModel,
    updateReplyState: (ReplyState) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    clearReplyContent: () -> Unit
): ReplyState.CanCreate {
    val reset = {
        updateReplyState(
            createCanCreateReplyState(
                coroutineScope,
                viewModel,
                updateReplyState,
                updateFailureState,
                clearReplyContent
            )
        )
    }

    return ReplyState.CanCreate(
        onCreateReply = { content ->
            val job = viewModel.createReply(content,
                onResponse = { failure ->
                    if (failure == null) {
                        updateReplyState(ReplyState.HasSentReply)
                        clearReplyContent()
                        coroutineScope.launch {
                            // Show success for 1 seconds. Then allow input of new reply.
                            delay(1.seconds)
                            reset()
                        }
                    } else {
                        updateFailureState(failure)
                        reset()
                    }
                }
            )
            updateReplyState(
                ReplyState.IsSendingReply(
                    onCancelSendReply = {
                        job.cancel()
                        updateReplyState(
                            createCanCreateReplyState(
                                coroutineScope,
                                viewModel,
                                updateReplyState,
                                updateFailureState,
                                clearReplyContent
                            )
                        )
                    }
                )
            )
        }
    )
}

@Composable
@Preview
private fun ReplyUiPreview() {
    var replyContent by remember { mutableStateOf("") }

    CreateReplyUi(
        modifier = Modifier.fillMaxWidth(),
        onReply = {},
        replyContent = replyContent,
        updateReplyContent = { replyContent = it }
    )
}