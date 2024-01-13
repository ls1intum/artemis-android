package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.rememberPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.shouldDisplayHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.PagingStateError
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import kotlinx.coroutines.Deferred
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.Date

internal const val TEST_TAG_METIS_POST_LIST = "TEST_TAG_METIS_POST_LIST"

internal fun testTagForPost(postId: Long) = "post$postId"

@Composable
internal fun MetisChatList(
    modifier: Modifier,
    viewModel: ConversationViewModel,
    listContentPadding: PaddingValues,
    state: LazyListState = rememberLazyListState(),
    isReplyEnabled: Boolean = true,
    onClickViewPost: (StandalonePostId) -> Unit
) {
    ReportVisibleMetisContext(remember(viewModel.metisContext) { VisiblePostList(viewModel.metisContext) })

    val posts: LazyPagingItems<ChatListItem> =
        viewModel.chatListUseCase.postPagingData.collectAsLazyPagingItems()
    val isDataOutdated by viewModel.isDataOutdated.collectAsState(initial = false)

    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()
    val hasModerationRights by viewModel.hasModerationRights.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()

    MetisChatList(
        modifier = modifier,
        initialReplyTextProvider = viewModel,
        state = state,
        isReplyEnabled = isReplyEnabled,
        posts = posts.asPostsDataState(),
        isDataOutdated = isDataOutdated,
        clientId = clientId,
        hasModerationRights = hasModerationRights,
        listContentPadding = listContentPadding,
        serverUrl = serverUrl,
        courseId = viewModel.courseId,
        onCreatePost = viewModel::createPost,
        onEditPost = viewModel::editPost,
        onDeletePost = viewModel::deletePost,
        onRequestReactWithEmoji = viewModel::createOrDeleteReaction,
        onClickViewPost = onClickViewPost,
        onRequestReload = viewModel::requestReload
    )
}

@Composable
fun MetisChatList(
    modifier: Modifier,
    initialReplyTextProvider: InitialReplyTextProvider,
    posts: PostsDataState,
    isDataOutdated: Boolean,
    clientId: Long,
    hasModerationRights: Boolean,
    listContentPadding: PaddingValues,
    serverUrl: String,
    courseId: Long,
    state: LazyListState,
    isReplyEnabled: Boolean,
    onCreatePost: () -> Deferred<MetisModificationFailure?>,
    onEditPost: (IStandalonePost, String) -> Deferred<MetisModificationFailure?>,
    onDeletePost: (IStandalonePost) -> Deferred<MetisModificationFailure?>,
    onRequestReactWithEmoji: (IStandalonePost, emojiId: String, create: Boolean) -> Deferred<MetisModificationFailure?>,
    onClickViewPost: (StandalonePostId) -> Unit,
    onRequestReload: () -> Unit
) {
    MetisReplyHandler(
        initialReplyTextProvider = initialReplyTextProvider,
        onCreatePost = onCreatePost,
        onEditPost = onEditPost,
        onDeletePost = onDeletePost,
        onRequestReactWithEmoji = onRequestReactWithEmoji
    ) { replyMode, onEditPostDelegate, onRequestReactWithEmojiDelegate, onDeletePostDelegate, updateFailureStateDelegate ->
        Column(modifier = modifier) {
            MetisOutdatedBanner(
                modifier = Modifier.fillMaxWidth(),
                isOutdated = isDataOutdated,
                requestRefresh = onRequestReload
            )

            val informationModifier = Modifier
                .fillMaxSize()
                .padding(16.dp)

            MetisPostListHandler(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                serverUrl = serverUrl,
                courseId = courseId,
                state = state,
                itemCount = posts.itemCount,
                order = DisplayPostOrder.REVERSED,
                getItem = posts::peek
            ) {
                when (posts) {
                    PostsDataState.Empty -> {
                        NoPostsFoundInformation(modifier = informationModifier)
                    }

                    PostsDataState.Loading -> {
                        LoadingPostsInformation(informationModifier)
                    }

                    is PostsDataState.Error -> {
                        PagingStateError(
                            modifier = informationModifier,
                            errorText = R.string.metis_post_list_error,
                            buttonText = R.string.metis_post_list_error_try_again,
                            retry = posts.retry
                        )
                    }

                    is PostsDataState.Loaded -> {
                        ChatList(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(TEST_TAG_METIS_POST_LIST),
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
    posts: PostsDataState.Loaded,
    hasModerationRights: Boolean,
    clientId: Long,
    onClickViewPost: (StandalonePostId) -> Unit,
    onRequestEdit: (IStandalonePost) -> Unit,
    onRequestDelete: (IStandalonePost) -> Unit,
    onRequestReactWithEmoji: (IStandalonePost, emojiId: String, create: Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = listContentPadding,
        state = state,
        reverseLayout = true
    ) {
        items(
            count = posts.itemCount,
            key = posts::getItemKey
        ) { index ->
            when (val chatListItem = posts[index]) {
                is ChatListItem.DateDivider -> {
                    DateDivider(
                        modifier = Modifier.fillMaxWidth(),
                        date = chatListItem.localDate
                    )
                }

                is ChatListItem.PostChatListItem? -> {
                    val post = chatListItem?.post

                    val postActions = rememberPostActions(
                        post = post,
                        hasModerationRights = hasModerationRights,
                        clientId = clientId,
                        onRequestEdit = { onRequestEdit(post ?: return@rememberPostActions) },
                        onRequestDelete = {
                            onRequestDelete(post ?: return@rememberPostActions)
                        },
                        onClickReaction = { id, create ->
                            onRequestReactWithEmoji(post ?: return@rememberPostActions, id, create)
                        },
                        onReplyInThread = {
                            onClickViewPost(post?.standalonePostId ?: return@rememberPostActions)
                        }
                    )

                    PostWithBottomSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (post != null) {
                                    it.testTag(testTagForPost(post.serverPostId))
                                } else it
                            },
                        post = post,
                        clientId = clientId,
                        postItemViewType = remember(post?.answers) {
                            PostItemViewType.ChatListItem(post?.answers.orEmpty())
                        },
                        postActions = postActions,
                        displayHeader = shouldDisplayHeader(
                            index = index,
                            post = post,
                            postCount = posts.itemCount,
                            order = DisplayPostOrder.REVERSED,
                            getPost = { getPostIndex ->
                                when (val entry = posts.peek(getPostIndex)) {
                                    is ChatListItem.PostChatListItem -> entry.post
                                    else -> null
                                }
                            }
                        ),
                        onClick = {
                            if (post != null) {
                                onClickViewPost(post.standalonePostId)
                            }
                        }
                    )
                }

                null -> {} // Not reachable but required by compiler
            }
        }

        val appendState = posts.appendState

        if (appendState is PostsDataState.Loading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.4f))
            }
        }

        if (appendState is PostsDataState.Error) {
            item {
                PagingStateError(
                    modifier = Modifier.fillMaxWidth(),
                    errorText = R.string.metis_post_list_error,
                    buttonText = R.string.metis_post_list_error_try_again,
                    retry = appendState.retry
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