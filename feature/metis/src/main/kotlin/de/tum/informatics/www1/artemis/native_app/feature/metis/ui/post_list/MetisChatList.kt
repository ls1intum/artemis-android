package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.PagingStateError
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.DisplayHeaderOrder
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.getPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.shouldDisplayHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisiblePostList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.Date

@Composable
internal fun MetisChatList(
    modifier: Modifier,
    viewModel: MetisListViewModel,
    listContentPadding: PaddingValues,
    state: LazyListState = rememberLazyListState(),
    isReplyEnabled: Boolean = true,
    onClickViewPost: (clientPostId: String) -> Unit
) {
    ReportVisibleMetisContext(remember(viewModel.metisContext) { VisiblePostList(viewModel.metisContext) })

    val posts: LazyPagingItems<ChatListItem> = viewModel.postPagingData.collectAsLazyPagingItems()
    val isDataOutdated by viewModel.isDataOutdated.collectAsState(initial = false)

    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()
    val hasModerationRights by viewModel.hasModerationRights.collectAsState()

    MetisReplyHandler(
        onCreatePost = viewModel::createPost,
        onEditPost = viewModel::editPost,
        onDeletePost = viewModel::deletePost,
        onRequestReactWithEmoji = viewModel::createOrDeleteReaction
    ) { replyMode, onEditPostDelegate, onRequestReactWithEmojiDelegate, onDeletePostDelegate, updateFailureStateDelegate ->
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
                        ProvideMarkwon {
                            ProvideEmojis {
                                ChatList(
                                    modifier = Modifier.fillMaxSize(),
                                    listContentPadding = listContentPadding,
                                    state = state,
                                    posts = posts,
                                    clientId = clientId,
                                    onClickViewPost = onClickViewPost,
                                    hasModerationRights = hasModerationRights,
                                    onRequestEdit = onEditPostDelegate,
                                    onRequestDelete = onDeletePostDelegate,
                                    onRequestReactWithEmoji = onRequestReactWithEmojiDelegate
                                )
                            }
                        }
                    }
                }
            }

            if (isReplyEnabled) {
                ReplyTextField(
                    modifier = Modifier.fillMaxWidth(),
                    replyMode = replyMode,
                    updateFailureState = updateFailureStateDelegate
                )
            }
        }
    }
}

@Composable
private fun ChatList(
    modifier: Modifier,
    listContentPadding: PaddingValues,
    state: LazyListState,
    posts: LazyPagingItems<ChatListItem>,
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
        state = state,
        reverseLayout = true
    ) {
        itemsIndexed(
            items = posts,
            key = { _, chatListItem ->
                when (chatListItem) {
                    is ChatListItem.DateDivider -> chatListItem.localDate.toEpochDays()
                    is ChatListItem.PostChatListItem -> chatListItem.post.clientPostId
                }
            }
        ) { index, chatListItem ->
            when (chatListItem) {
                is ChatListItem.DateDivider -> {
                    DateDivider(
                        modifier = Modifier.fillMaxWidth(),
                        date = chatListItem.localDate
                    )
                }

                is ChatListItem.PostChatListItem? -> {
                    val post = chatListItem?.post

                    val postActions =
                        remember(
                            chatListItem,
                            hasModerationRights,
                            clientId,
                            onRequestEdit,
                            onRequestDelete
                        ) {
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
                        postItemViewType = remember(post?.answers) {
                            PostItemViewType.ChatListItem(
                                post?.answers.orEmpty()
                            )
                        },
                        postActions = postActions,
                        displayHeader = shouldDisplayHeader(
                            index = index,
                            post = post,
                            postCount = posts.itemCount,
                            order = DisplayHeaderOrder.REVERSED,
                            getPost = { getPostIndex ->
                                when (val entry = posts.peek(getPostIndex)) {
                                    is ChatListItem.PostChatListItem -> entry.post
                                    else -> null
                                }
                            }
                        ),
                        onClick = {
                            if (post != null) {
                                onClickViewPost(post.clientPostId)
                            }
                        }
                    )
                }

                null -> {} // Not reachable but required by compiler
            }

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

private val DateFormat = SimpleDateFormat.getDateInstance()

@Composable
private fun DateDivider(modifier: Modifier, date: LocalDate) {
    val dateAsString = remember(date) {
        DateFormat.format(
            Date.from(
                date.atStartOfDayIn(TimeZone.currentSystemDefault()).toJavaInstant()
            )
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f))

        Text(
            text = dateAsString,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Divider(modifier = Modifier.weight(1f))
    }
}