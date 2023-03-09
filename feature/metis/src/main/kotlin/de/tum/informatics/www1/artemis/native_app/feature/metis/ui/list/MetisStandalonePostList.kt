package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.*
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisService

@Composable
internal fun MetisStandalonePostList(
    modifier: Modifier,
    viewModel: MetisListViewModel,
    listContentPadding: PaddingValues,
    state: LazyListState = rememberLazyListState(),
    onClickViewPost: (clientPostId: String) -> Unit,
    onClickViewReplies: (clientPostId: String) -> Unit,
    onClickCreatePost: (() -> Unit)?
) {
    val posts: LazyPagingItems<Post> = viewModel.postPagingData.collectAsLazyPagingItems()
    val isDataOutdated = viewModel.isDataOutdated.collectAsState(initial = false).value

    var metisFailure: de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure? by remember { mutableStateOf(null) }

    val clientId: Long = viewModel.clientId.collectAsState().value.orElse(0L)

    Column(modifier = modifier) {
        MetisOutdatedBanner(
            modifier = Modifier.fillMaxWidth(),
            isOutdated = isDataOutdated,
            requestRefresh = viewModel::requestReload
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val informationModifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
            when {
                posts.itemCount == 0 &&
                        (posts.loadState.refresh.endOfPaginationReached || posts.loadState.append.endOfPaginationReached) -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        onClickCreatePost?.let {
                            CreatePostButton(modifier = Modifier, onClick = it)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.metis_post_list_empty),
                                modifier = informationModifier,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                posts.loadState.refresh is LoadState.Loading -> {
                    Column(
                        modifier = informationModifier,
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

                posts.loadState.refresh is LoadState.Error -> {
                    PageStateError(modifier = informationModifier, retry = posts::retry)
                }

                else -> {
                    ProvideEmojis {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = listContentPadding,
                            state = state
                        ) {
                            onClickCreatePost?.let {
                                item {
                                    CreatePostButton(modifier = Modifier, onClick = it)
                                }
                            }

                            items(posts, key = { it.clientPostId }) { post ->
                                val asAffectedPost = post?.let {
                                    MetisModificationService.AffectedPost.Standalone(it.serverPostId)
                                }
                                PostItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    post = post,
                                    clientId = clientId,
                                    postItemViewType = PostItemViewType.StandaloneListItem(
                                        answerPosts = post?.answers.orEmpty(),
                                        onClickReply = {
                                            if (post != null) {
                                                onClickViewPost(post.clientPostId)
                                            }
                                        },
                                        onClickViewReplies = {
                                            if (post != null) {
                                                onClickViewReplies(post.clientPostId)
                                            }
                                        },
                                        onClickPost = {
                                            if (post != null) {
                                                onClickViewPost(post.clientPostId)
                                            }
                                        },
                                        // TODO: Implement permission rights once refactored on server side
                                        canEdit = false,
                                        canDelete = false,
                                        onClickDelete = {},
                                        onClickEdit = {}
                                    ),
                                    onReactWithEmoji = { emojiId ->
                                        viewModel.createReaction(
                                            emojiId = emojiId,
                                            post = asAffectedPost ?: return@PostItem
                                        ) { metisFailure = it }
                                    },
                                    onClickOnPresentReaction = { emojiId ->
                                        viewModel.onClickReaction(
                                            emojiId = emojiId,
                                            post = asAffectedPost ?: return@PostItem,
                                            presentReactions = post.reactions,
                                        ) { metisFailure = it }
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
                                    PageStateError(
                                        modifier = Modifier.fillMaxWidth(),
                                        retry = posts::retry
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
    }
}

@Composable
private fun CreatePostButton(modifier: Modifier, onClick: () -> Unit) {
    Button(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )

        Text(text = stringResource(id = R.string.communication_create_post_button))
    }
}

@Composable
private fun PageStateError(
    modifier: Modifier,
    retry: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.metis_post_list_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(onClick = retry) {
            Text(text = stringResource(id = R.string.metis_post_list_error_try_again))
        }
    }
}