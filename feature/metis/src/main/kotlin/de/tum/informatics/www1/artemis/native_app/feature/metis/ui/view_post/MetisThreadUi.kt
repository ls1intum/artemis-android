package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.getPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.ReplyMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleStandalonePostDetails

/**
 * Displays a single post with its replies.
 * @param viewType is currently unused, but should be used to scroll to the requested content in the future.
 */
@Composable
internal fun MetisThreadUi(
    modifier: Modifier,
    viewModel: MetisThreadViewModel,
    @Suppress("UNUSED_PARAMETER") viewType: ViewType
) {
    val postDataState: DataState<Post> by viewModel.post.collectAsState()
    val clientId: Long = viewModel.clientId.collectAsState().value.orElse(0L)

    val hasModerationRights = viewModel.hasModerationRights.collectAsState().value.orElse(false)

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
                        hasModerationRights = hasModerationRights,
                        clientId = clientId
                    ) { metisFailure = it }
                }

                var replyMode: ReplyMode by remember {
                    mutableStateOf(
                        ReplyMode.NewMessage(onCreateNewMessage = viewModel::createReply)
                    )
                }

                AnimatedVisibility(visible = postDataState.isSuccess) {
                    ReplyTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = this@BoxWithConstraints.maxHeight * 0.6f),
                        replyMode = replyMode,
                        updateFailureState = { metisFailure = it }
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
    hasModerationRights: Boolean,
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

            val postActions = remember(post, hasModerationRights, clientId) {
                if (post != null) {
                    getPostActions(
                        post = post,
                        hasModerationRights = hasModerationRights,
                        clientId = clientId,
                        onRequestEdit = {},
                        onRequestDelete = {},
                        onRequestReactWithEmoji = {}
                    )
                } else PostActions()
            }

            PostWithBottomSheet(
                modifier = Modifier.padding(top = 8.dp),
                post = post,
                postItemViewType = PostItemViewType.ThreadAnswerItem,
                postActions = postActions,
//                onRequestReactWithEmoji = { emojiId ->
//                    viewModel.createReaction(
//                        emojiId = emojiId,
//                        post = asAffectedPost,
//                        response = setMetisFailure
//                    )
//                },
//                onClickOnPresentReaction = { emojiId ->
//                    viewModel.onClickReaction(
//                        emojiId = emojiId,
//                        post = asAffectedPost,
//                        presentReactions = post.reactions,
//                        response = setMetisFailure
//                    )
//                },
                clientId = clientId,
                onClick = {}
            )
        }

        items(post.orderedAnswerPostings, key = { it.postId }) { answerPost ->
            val postActions = remember(answerPost, hasModerationRights, clientId) {
                getPostActions(
                    post = answerPost,
                    hasModerationRights = hasModerationRights,
                    clientId = clientId,
                    onRequestEdit = {},
                    onRequestDelete = {},
                    onRequestReactWithEmoji = {}
                )
            }

            PostWithBottomSheet(
                modifier = Modifier.fillMaxWidth(),
                post = answerPost,
                postActions = postActions,
//                onRequestReactWithEmoji = { emojiId ->
//                    viewModel.createReactionForAnswer(
//                        emojiId = emojiId,
//                        clientSidePostId = answerPost.postId,
//                        onResponse = setMetisFailure
//                    )
//                },
//                onClickOnPresentReaction = { emojiId ->
//                    viewModel.onClickReactionOfAnswer(
//                        emojiId = emojiId,
//                        clientSidePostId = answerPost.postId,
//                        presentReactions = answerPost.reactions,
//                        onResponse = setMetisFailure
//                    )
//                },
                postItemViewType = PostItemViewType.ThreadItem,
                clientId = clientId,
                onClick = {}
            )
        }
    }
}
