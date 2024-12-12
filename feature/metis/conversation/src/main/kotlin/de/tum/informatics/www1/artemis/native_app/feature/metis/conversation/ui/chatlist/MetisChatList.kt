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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.rememberPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.shouldDisplayHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.PagingStateError
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import kotlinx.coroutines.Deferred
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date

internal const val TEST_TAG_METIS_POST_LIST = "TEST_TAG_METIS_POST_LIST"

internal fun testTagForPost(postId: StandalonePostId?) = "post$postId"

@Composable
internal fun MetisChatList(
    modifier: Modifier,
    viewModel: ConversationViewModel,
    posts: LazyPagingItems<ChatListItem>,
    listContentPadding: PaddingValues,
    state: LazyListState = rememberLazyListState(),
    isReplyEnabled: Boolean = true,
    onClickViewPost: (StandalonePostId) -> Unit,
    title: String? = "Replying..."
) {
    ReportVisibleMetisContext(remember(viewModel.metisContext) { VisiblePostList(viewModel.metisContext) })

    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()
    val postActionFlags by viewModel.postActionFlags.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()

    val bottomItem: PostPojo? by viewModel.chatListUseCase.bottomPost.collectAsState()

    val conversationDataState by viewModel.latestUpdatedConversation.collectAsState()

    val updatedTitle by remember(conversationDataState) {
        derivedStateOf {
            conversationDataState.bind { it.humanReadableName }.orElse("Conversation")
        }
    }

    ProvideMarkwon {
        MetisChatList(
            modifier = modifier,
            initialReplyTextProvider = viewModel,
            posts = posts.asPostsDataState(),
            clientId = clientId,
            postActionFlags = postActionFlags,
            listContentPadding = listContentPadding,
            serverUrl = serverUrl,
            courseId = viewModel.courseId,
            state = state,
            bottomItem = bottomItem,
            isReplyEnabled = isReplyEnabled,
            onCreatePost = viewModel::createPost,
            onEditPost = viewModel::editPost,
            onDeletePost = viewModel::deletePost,
            onPinPost = viewModel::togglePinPost,
            onRequestReactWithEmoji = viewModel::createOrDeleteReaction,
            onClickViewPost = onClickViewPost,
            onRequestRetrySend = viewModel::retryCreatePost,
            title = updatedTitle
        )
    }
}

@Composable
fun MetisChatList(
    modifier: Modifier,
    initialReplyTextProvider: InitialReplyTextProvider,
    posts: PostsDataState,
    bottomItem: PostPojo?,
    clientId: Long,
    postActionFlags: PostActionFlags,
    listContentPadding: PaddingValues,
    serverUrl: String,
    courseId: Long,
    state: LazyListState,
    emojiService: EmojiService = koinInject(),
    isReplyEnabled: Boolean,
    onCreatePost: () -> Deferred<MetisModificationFailure?>,
    onEditPost: (IStandalonePost, String) -> Deferred<MetisModificationFailure?>,
    onDeletePost: (IStandalonePost) -> Deferred<MetisModificationFailure?>,
    onPinPost: (IStandalonePost) -> Deferred<MetisModificationFailure?>,
    onRequestReactWithEmoji: (IStandalonePost, emojiId: String, create: Boolean) -> Deferred<MetisModificationFailure?>,
    onClickViewPost: (StandalonePostId) -> Unit,
    onRequestRetrySend: (StandalonePostId) -> Unit,
    title: String
) {
    MetisReplyHandler(
        initialReplyTextProvider = initialReplyTextProvider,
        onCreatePost = onCreatePost,
        onEditPost = onEditPost,
        onResolvePost = null,
        onPinPost = onPinPost,
        onDeletePost = onDeletePost,
        onRequestReactWithEmoji = onRequestReactWithEmoji,
    ) { replyMode, onEditPostDelegate, _, onRequestReactWithEmojiDelegate, onDeletePostDelegate, onPinPostDelegate, updateFailureStateDelegate ->
        Column(modifier = modifier) {
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
                emojiService = emojiService,
                bottomItem = bottomItem
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
                            postActionFlags = postActionFlags,
                            onRequestEdit = onEditPostDelegate,
                            onRequestDelete = onDeletePostDelegate,
                            onRequestPin = onPinPostDelegate,
                            onRequestReactWithEmoji = onRequestReactWithEmojiDelegate,
                            onRequestRetrySend = onRequestRetrySend
                        )
                    }
                }
            }

            if (isReplyEnabled) {
                ReplyTextField(
                    modifier = Modifier.fillMaxWidth(),
                    replyMode = replyMode,
                    updateFailureState = updateFailureStateDelegate,
                    title = title,
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
    postActionFlags: PostActionFlags,
    clientId: Long,
    onClickViewPost: (StandalonePostId) -> Unit,
    onRequestEdit: (IStandalonePost) -> Unit,
    onRequestDelete: (IStandalonePost) -> Unit,
    onRequestPin: (IStandalonePost) -> Unit,
    onRequestReactWithEmoji: (IStandalonePost, emojiId: String, create: Boolean) -> Unit,
    onRequestRetrySend: (StandalonePostId) -> Unit
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
                        postActionFlags = postActionFlags,
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
                        },
                        onResolvePost = null,
                        onPinPost = { onRequestPin(post ?: return@rememberPostActions) },
                        onRequestRetrySend = {
                            onRequestRetrySend(
                                post?.standalonePostId ?: return@rememberPostActions
                            )
                        }
                    )

                    PostWithBottomSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (post != null) {
                                    it.testTag(testTagForPost(post.standalonePostId))
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
                            val standalonePostId = post?.standalonePostId

                            if (post?.serverPostId != null && standalonePostId != null) {
                                onClickViewPost(standalonePostId)
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