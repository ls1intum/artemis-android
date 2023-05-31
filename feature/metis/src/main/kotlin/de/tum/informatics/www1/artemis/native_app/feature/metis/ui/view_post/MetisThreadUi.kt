package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.AnswerPostDb
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.*
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.isReplyEnabled
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.getPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleStandalonePostDetails
import kotlinx.coroutines.CompletableDeferred

/**
 * Displays a single post with its replies.
 */
@Composable
internal fun MetisThreadUi(
    modifier: Modifier,
    viewModel: MetisThreadViewModel
) {
    val postDataState: DataState<Post> by viewModel.post.collectAsState()
    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()

    val hasModerationRights by viewModel.hasModerationRights.collectAsState()

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

    val conversationDataState by viewModel.conversation.collectAsState()
    val isReplyEnabled = isReplyEnabled(conversationDataState = conversationDataState)

    ProvideEmojis {
        MetisReplyHandler(
            onCreatePost = viewModel::createReply,
            onEditPost = { post, newText ->
                val parentPost = postDataState.orNull()

                when (post) {
                    is AnswerPostDb -> {
                        if (parentPost == null) return@MetisReplyHandler CompletableDeferred(
                            MetisModificationFailure.UPDATE_POST
                        )
                        viewModel.editAnswerPost(parentPost, post, newText)
                    }

                    is Post -> viewModel.editPost(post, newText)
                    else -> throw NotImplementedError()
                }
            },
            onDeletePost = viewModel::deletePost,
            onRequestReactWithEmoji = viewModel::createOrDeleteReaction
        ) { replyMode, onEditPostDelegate, onRequestReactWithEmojiDelegate, onDeletePostDelegate, updateFailureStateDelegate ->
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
                            clientId = clientId,
                            onRequestReactWithEmoji = onRequestReactWithEmojiDelegate,
                            onRequestEdit = onEditPostDelegate,
                            onRequestDelete = onDeletePostDelegate
                        )
                    }

                    AnimatedVisibility(visible = postDataState.isSuccess && isReplyEnabled) {
                        ReplyTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.6f),
                            replyMode = replyMode,
                            updateFailureState = updateFailureStateDelegate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostAndRepliesList(
    modifier: Modifier,
    post: Post,
    hasModerationRights: Boolean,
    clientId: Long,
    onRequestEdit: (IBasePost) -> Unit,
    onRequestDelete: (IBasePost) -> Unit,
    onRequestReactWithEmoji: (IBasePost, emojiId: String, create: Boolean) -> Unit
) {
    val rememberPostActions: @Composable (IBasePost) -> PostActions = { affectedPost: IBasePost ->
        remember(affectedPost, hasModerationRights, clientId) {
            getPostActions(
                affectedPost,
                hasModerationRights,
                clientId,
                onRequestEdit = { onRequestEdit(affectedPost) },
                onRequestDelete = { onRequestDelete(affectedPost) },
                onClickReaction = { emojiId, create ->
                    onRequestReactWithEmoji(
                        affectedPost,
                        emojiId,
                        create
                    )
                }
            )
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val postActions = rememberPostActions(post)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostWithBottomSheet(
                    modifier = Modifier.padding(top = 8.dp),
                    post = post,
                    postItemViewType = PostItemViewType.ThreadContextPostItem,
                    postActions = postActions,
                    clientId = clientId,
                    onClick = {}
                )

                Divider()

                Box {}
            }
        }

        items(post.orderedAnswerPostings, key = { it.postId }) { answerPost ->
            val postActions = rememberPostActions(answerPost)

            PostWithBottomSheet(
                modifier = Modifier.fillMaxWidth(),
                post = answerPost,
                postActions = postActions,
                postItemViewType = PostItemViewType.ThreadAnswerItem,
                clientId = clientId,
                onClick = {}
            )
        }
    }
}
