package de.tum.informatics.www1.artemis.native_app.core.communication.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisModificationFailureDialog
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getEmojiForEmojiId
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import org.koin.androidx.compose.get

@Composable
internal fun MetisStandalonePostList(
    modifier: Modifier,
    viewModel: MetisListViewModel,
    onClickViewPost: (clientPostId: String) -> Unit,
    onClickViewReplies: (clientPostId: String) -> Unit
) {
    val posts: LazyPagingItems<Post> = viewModel.postPagingData.collectAsLazyPagingItems()
    val isDataOutdated = viewModel.isDataOutdated.collectAsState(initial = false).value

    var metisFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    val clientId: Long = viewModel.clientId.collectAsState().value.orElse(0L)

    Column(modifier = modifier) {
        MetisOutdatedBanner(
            modifier = Modifier.fillMaxWidth(),
            isOutdated = isDataOutdated,
            requestRefresh = viewModel::requestRefreshWebsocket
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
                    Text(
                        text = stringResource(id = R.string.metis_post_list_empty),
                        modifier = informationModifier,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(posts, key = { it.clientPostId }) { post ->
                            val asAffectedPost = post?.let {
                                MetisService.AffectedPost.Standalone(it.serverPostId)
                            }
                            PostItem(
                                modifier = Modifier.fillMaxWidth(),
                                post = post,
                                clientId = clientId,
                                postItemViewType = PostItemViewType.StandaloneListItem(
                                    answerPosts = post?.answerPostings.orEmpty(),
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
                                getUnicodeForEmojiId = {
                                    val emoji = getEmojiForEmojiId(emojiId = it)
                                    emoji
                                },
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

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
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