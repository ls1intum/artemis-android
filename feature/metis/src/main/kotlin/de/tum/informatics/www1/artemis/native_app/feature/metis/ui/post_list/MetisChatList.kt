package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisModificationFailureDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.PagingStateError
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.getPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.rememberReplyMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisiblePostList
import kotlinx.coroutines.Deferred

@Composable
internal fun MetisChatList(
    modifier: Modifier,
    viewModel: MetisListViewModel,
    listContentPadding: PaddingValues,
    state: LazyListState = rememberLazyListState(),
    onClickViewPost: (clientPostId: String) -> Unit
) {
    ReportVisibleMetisContext(remember(viewModel.metisContext) { VisiblePostList(viewModel.metisContext) })

    val posts: LazyPagingItems<Post> = viewModel.postPagingData.collectAsLazyPagingItems()
    val isDataOutdated = viewModel.isDataOutdated.collectAsState(initial = false).value

    val clientId: Long = viewModel.clientId.collectAsState().value.orElse(0L)
    val hasModerationRights = viewModel.hasModerationRights.collectAsState().value.orElse(false)

    var metisFailure: MetisModificationFailure? by remember {
        mutableStateOf(null)
    }

    var metisModificationTask: Deferred<MetisModificationFailure?>? by remember {
        mutableStateOf(null)
    }
    var editingPost: Post? by remember { mutableStateOf(null) }
    val replyMode = rememberReplyMode(
        editingPost = editingPost,
        onClearEditingPost = { editingPost = null },
        onCreatePost = viewModel::createPost,
        onEditPost = viewModel::editPost
    )

    AwaitDeferredCompletion(job = metisModificationTask) {
        metisFailure = it
    }

    Column(modifier = modifier) {
        MetisOutdatedBanner(
            modifier = Modifier.fillMaxWidth(),
            isOutdated = isDataOutdated,
            requestRefresh = viewModel::requestReload
        )

        val informationModifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                posts.itemCount == 0 -> {
                    NoPostsFoundInformation(modifier = informationModifier)
                }

                posts.loadState.refresh is LoadState.Loading -> {
                    LoadingPostsInformation(informationModifier)
                }

                posts.loadState.refresh is LoadState.Error -> {
                    PagingStateError(
                        modifier = informationModifier,
                        errorText = R.string.metis_post_list_error,
                        buttonText = R.string.metis_post_list_error_try_again,
                        retry = posts::retry
                    )
                }

                else -> {
                    ProvideEmojis {
                        ChatList(
                            modifier = Modifier.fillMaxSize(),
                            listContentPadding = listContentPadding,
                            state = state,
                            posts = posts,
                            clientId = clientId,
                            onClickViewPost = onClickViewPost,
                            hasModerationRights = hasModerationRights,
                            onRequestEdit = { post -> editingPost = post },
                            onRequestDelete = { post ->
                                metisModificationTask = viewModel.deletePost(post)
                            },
                            onRequestReactWithEmoji = { post, emojiId, create ->
                                metisModificationTask = viewModel.createOrDeleteReaction(emojiId, post, create)
                            }
                        )
                    }
                }
            }
        }

        ReplyTextField(
            modifier = Modifier.fillMaxWidth(),
            replyMode = replyMode,
            updateFailureState = { metisFailure = it }
        )
    }

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
    }
}

@Composable
private fun ChatList(
    modifier: Modifier,
    listContentPadding: PaddingValues,
    state: LazyListState,
    posts: LazyPagingItems<Post>,
    hasModerationRights: Boolean,
    clientId: Long,
    onClickViewPost: (clientPostId: String) -> Unit,
    onRequestEdit: (Post) -> Unit,
    onRequestDelete: (Post) -> Unit,
    onRequestReactWithEmoji: (Post, emojiId: String, create: Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = listContentPadding,
        state = state
    ) {
        items(posts, key = { it.clientPostId }) { post ->
            val postActions =
                remember(post, hasModerationRights, clientId, onRequestEdit, onRequestDelete) {
                    if (post != null) {
                        getPostActions(
                            post = post,
                            hasModerationRights = hasModerationRights,
                            clientId = clientId,
                            onRequestEdit = { onRequestEdit(post) },
                            onRequestDelete = { onRequestDelete(post) },
                            onClickReaction = { id, create ->
                                onRequestReactWithEmoji(post, id, create)
                            }
                        )
                    } else PostActions()
                }

            PostWithBottomSheet(
                modifier = Modifier.fillMaxWidth(),
                post = post,
                clientId = clientId,
                postItemViewType = PostItemViewType.ChatListItem(post?.answers.orEmpty()),
                postActions = postActions,
                onClick = {
                    if (post != null) {
                        onClickViewPost(post.clientPostId)
                    }
                }
            )
        }

        if (posts.loadState.append is LoadState.Loading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.4f))
            }
        }

        if (posts.loadState.append is LoadState.Error) {
            item {
                PagingStateError(
                    modifier = Modifier.fillMaxWidth(),
                    errorText = R.string.metis_post_list_error,
                    buttonText = R.string.metis_post_list_error_try_again,
                    retry = posts::retry
                )
            }
        }
    }
}

@Composable
private fun LoadingPostsInformation(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.metis_post_list_loading),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.4f)
        )
    }
}

@Composable
private fun NoPostsFoundInformation(
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.metis_post_list_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
