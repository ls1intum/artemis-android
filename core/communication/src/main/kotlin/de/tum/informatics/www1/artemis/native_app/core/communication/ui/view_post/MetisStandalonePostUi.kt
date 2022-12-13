package de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.AnswerPostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisModificationFailureDialog
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.common.MarkdownTextField
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getEmojiForEmojiId
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.util.clientId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun MetisStandalonePostUi(
    modifier: Modifier,
    viewModel: MetisStandalonePostViewModel,
    viewType: ViewType
) {
    val accountService: AccountService = get()

    val post: Post? = viewModel.post.collectAsState(initial = null).value
    val clientId: Int by accountService.clientId.collectAsState(initial = -1)

    var metisFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val asAffectedPost = post?.let {
                    MetisService.AffectedPost.Standalone(
                        postId = it.serverPostId
                    )
                }

                PostItem(
                    modifier = Modifier.padding(top = 8.dp),
                    post = post,
                    postItemViewType = PostItemViewType.StandaloneView,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) },
                    onReactWithEmoji = { emojiId ->
                        viewModel.createReaction(
                            emojiId = emojiId,
                            post = asAffectedPost ?: return@PostItem,
                            response = { metisFailure = it }
                        )
                    },
                    onClickOnPresentReaction = { emojiId ->
                        viewModel.onClickReaction(
                            emojiId = emojiId,
                            post = asAffectedPost ?: return@PostItem,
                            presentReactions = post.reactions,
                            response = { metisFailure = it }
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

            items(post?.orderedAnswerPostings.orEmpty(), key = { it.postId }) { answerPost ->
                AnswerPostItem(
                    modifier = Modifier.fillMaxWidth(),
                    answerPost = answerPost,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) },
                    onReactWithEmoji = { emojiId ->
                        viewModel.createReactionForAnswer(
                            emojiId = emojiId,
                            clientSidePostId = answerPost.postId,
                            onResponse = { metisFailure = it }
                        )
                    },
                    onClickOnPresentReaction = { emojiId ->
                        viewModel.onClickReactionOfAnswer(
                            emojiId = emojiId,
                            clientSidePostId = answerPost.postId,
                            presentReactions = answerPost.reactions,
                            onResponse = { metisFailure = it }
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

        ReplyUi(
            modifier = Modifier.fillMaxWidth(),
            replyState = replyState,
            replyContent = replyContent,
            updateReplyContent = { replyContent = it }
        )
    }

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
    }
}

@Composable
private fun ReplyUi(
    modifier: Modifier,
    replyContent: String,
    updateReplyContent: (String) -> Unit,
    replyState: ReplyState
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(5))
                .background(MaterialTheme.colorScheme.secondaryContainer)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkdownTextField(
                    modifier = Modifier.weight(1f),
                    text = replyContent,
                    onTextChanged = updateReplyContent,
                    focusRequester = focusRequest,
                    onFocusLost = {
                        if (displayTextField && replyContent.isEmpty()) {
                            displayTextField = false
                        }
                    }
                )

                IconButton(
                    onClick = { onReply(replyContent) },
                    enabled = replyContent.isNotBlank()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                }
            }

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