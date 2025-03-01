package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisPostListHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.testTagForPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostItemViewJoinedType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.determinePostItemViewJoinedType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionBar
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.rememberPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.shouldDisplayHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.isReplyEnabled
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.util.rememberDerivedConversationName
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

internal const val TEST_TAG_THREAD_LIST = "TEST_TAG_THREAD_LIST"
internal fun testTagForAnswerPost(answerPostId: String?) = "answerPost$answerPostId"

/**
 * Displays a single post with its replies.
 */
@Composable
internal fun MetisThreadUi(
    modifier: Modifier,
    viewModel: ConversationViewModel
) {
    val postDataState: DataState<IStandalonePost> by viewModel.threadUseCase.post.collectAsState()
    val chatListContextItem by viewModel.threadUseCase.chatListItem.collectAsState()
    val answerChatListItemState: (IAnswerPost) -> StateFlow<ChatListItem.PostItem.ThreadItem.Answer?> =
        { viewModel.threadUseCase.getAnswerChatListItem(it) }

    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()

    val postActionFlags by viewModel.postActionFlags.collectAsState()

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

    ProvideMarkwon {
        MetisThreadUi(
            modifier = modifier,
            courseId = viewModel.courseId,
            initialReplyTextProvider = viewModel,
            conversationDataState = conversationDataState,
            postDataState = postDataState,
            chatListContextItem = chatListContextItem,
            postActionFlags = postActionFlags,
            serverUrl = serverUrl,
            emojiService = koinInject(),
            isMarkedAsDeleteList = viewModel.isMarkedAsDeleteList,
            clientId = clientId,
            answerChatListItemState = answerChatListItemState,
            onCreatePost = viewModel::createReply,
            onEditPost = { post, newText ->
                val parentPost = postDataState.orNull()

                when (post) {
                    is AnswerPostPojo -> {
                        if (parentPost == null) CompletableDeferred(
                            MetisModificationFailure.UPDATE_POST
                        ) else viewModel.editAnswerPost(parentPost, post, newText)
                    }

                    is PostPojo -> viewModel.editPost(post, newText)
                    else -> throw NotImplementedError()
                }
            },
            onResolvePost = { post ->
                val parentPost = postDataState.orNull()

                if (post is AnswerPostPojo) {
                    if (parentPost == null) CompletableDeferred(
                        MetisModificationFailure.UPDATE_POST
                    ) else viewModel.toggleResolvePost(parentPost, post)
                } else {
                    throw NotImplementedError()
                }
            },
            onPinPost = { post ->
                if (post is PostPojo) {
                    viewModel.togglePinPost(post)
                } else {
                    throw NotImplementedError()
                }
            },
            onSavePost = viewModel::toggleSavePost,
            onDeletePost = viewModel::deletePost,
            onUndoDeletePost = viewModel::undoDeletePost,
            onRequestReactWithEmoji = viewModel::createOrDeleteReaction,
            onRequestReload = viewModel::requestReload,
            onRequestRetrySend = viewModel::retryCreateReply,
            onFileSelect = { uri, context ->
                viewModel.onFileSelected(uri, context)
            }
        )
    }
}

@Composable
internal fun MetisThreadUi(
    modifier: Modifier,
    courseId: Long,
    clientId: Long,
    postDataState: DataState<IStandalonePost>,
    conversationDataState: DataState<Conversation>,
    chatListContextItem: ChatListItem.PostItem.ThreadItem?,
    postActionFlags: PostActionFlags,
    serverUrl: String,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    emojiService: EmojiService,
    initialReplyTextProvider: InitialReplyTextProvider,
    answerChatListItemState: (IAnswerPost) -> StateFlow<ChatListItem.PostItem.ThreadItem.Answer?>,
    onCreatePost: () -> Deferred<MetisModificationFailure?>,
    onEditPost: (IBasePost, String) -> Deferred<MetisModificationFailure?>,
    onResolvePost: ((IBasePost) -> Deferred<MetisModificationFailure?>)?,
    onPinPost: ((IBasePost) -> Deferred<MetisModificationFailure?>)?,
    onSavePost: ((IBasePost) -> Deferred<MetisModificationFailure?>)?,
    onDeletePost: (IBasePost) -> Deferred<MetisModificationFailure?>,
    onUndoDeletePost: (IBasePost) -> Unit,
    onRequestReactWithEmoji: (IBasePost, emojiId: String, create: Boolean) -> Deferred<MetisModificationFailure?>,
    onRequestReload: () -> Unit,
    onRequestRetrySend: (clientSidePostId: String, content: String) -> Unit,
    onFileSelect: (Uri, Context) -> Unit
) {
    val listState = rememberLazyListState()
    val isReplyEnabled = isReplyEnabled(conversationDataState = conversationDataState)
    val context = LocalContext.current
    val conversationName by rememberDerivedConversationName(conversationDataState)

    ProvideEmojis(emojiService) {
        MetisReplyHandler(
            initialReplyTextProvider = initialReplyTextProvider,
            onCreatePost = onCreatePost,
            onEditPost = onEditPost,
            onResolvePost = onResolvePost,
            onDeletePost = onDeletePost,
            onPinPost = onPinPost,
            onSavePost = onSavePost,
            onRequestReactWithEmoji = onRequestReactWithEmoji
        ) { replyMode, onEditPostDelegate, onResolvePostDelegate, onRequestReactWithEmojiDelegate, onDeletePostDelegate, onPinPostDelegate, onSavePostDelegate, updateFailureStateDelegate ->
            BoxWithConstraints(modifier = modifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    BasicDataStateUi(
                        modifier = Modifier
                            .weight(1f),
                        dataState = postDataState,
                        enablePullToRefresh = false,
                        loadingText = stringResource(id = R.string.standalone_post_loading),
                        failureText = stringResource(id = R.string.standalone_post_failure),
                        retryButtonText = stringResource(id = R.string.standalone_post_try_again),
                        onClickRetry = onRequestReload
                    ) { post ->
                        MetisPostListHandler(
                            modifier = Modifier
                                .fillMaxSize(),
                            serverUrl = serverUrl,
                            courseId = courseId,
                            state = listState,
                            itemCount = post.orderedAnswerPostings.size,
                            order = DisplayPostOrder.REGULAR,
                            emojiService = emojiService,
                            bottomItem = post.orderedAnswerPostings.lastOrNull()
                        ) {
                            PostAndRepliesList(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                                    .testTag(TEST_TAG_THREAD_LIST),
                                post = post,
                                chatListContextItem = chatListContextItem,
                                postActionFlags = postActionFlags,
                                clientId = clientId,
                                answerChatListItemState = answerChatListItemState,
                                isMarkedAsDeleteList = isMarkedAsDeleteList,
                                onRequestReactWithEmoji = onRequestReactWithEmojiDelegate,
                                onRequestEdit = onEditPostDelegate,
                                onRequestDelete = onDeletePostDelegate,
                                onRequestUndoDelete = onUndoDeletePost,
                                onRequestResolve = onResolvePostDelegate,
                                onRequestPin = onPinPostDelegate,
                                onRequestSave = onSavePostDelegate,
                                state = listState,
                                onRequestRetrySend = onRequestRetrySend
                            )
                        }
                    }

                    AnimatedVisibility(visible = postDataState.isSuccess && isReplyEnabled) {
                        ReplyTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.6f),
                            replyMode = replyMode,
                            updateFailureState = updateFailureStateDelegate,
                            conversationName = conversationName,
                            onFileSelected = { uri, ->
                                onFileSelect(uri, context)
                            }
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
    state: LazyListState,
    post: IStandalonePost,
    chatListContextItem: ChatListItem.PostItem.ThreadItem?,
    postActionFlags: PostActionFlags,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    clientId: Long,
    answerChatListItemState: (IAnswerPost) -> StateFlow<ChatListItem.PostItem.ThreadItem.Answer?>,
    onRequestEdit: (IBasePost) -> Unit,
    onRequestDelete: (IBasePost) -> Unit,
    onRequestUndoDelete: (IBasePost) -> Unit,
    onRequestResolve: (IBasePost) -> Unit,
    onRequestPin: (IBasePost) -> Unit,
    onRequestSave: (IBasePost) -> Unit,
    onRequestReactWithEmoji: (IBasePost, emojiId: String, create: Boolean) -> Unit,
    onRequestRetrySend: (clientSidePostId: String, content: String) -> Unit
) {
    val rememberPostActions: @Composable (IBasePost) -> PostActions = { affectedPost: IBasePost ->
        rememberPostActions(
            affectedPost,
            postActionFlags,
            clientId,
            onRequestEdit = { onRequestEdit(affectedPost) },
            onRequestDelete = { onRequestDelete(affectedPost) },
            onRequestUndoDelete = { onRequestUndoDelete(affectedPost) },
            onClickReaction = { emojiId, create ->
                onRequestReactWithEmoji(
                    affectedPost,
                    emojiId,
                    create
                )
            },
            onReplyInThread = null,
            onResolvePost = { onRequestResolve(affectedPost) },
            onPinPost = { onRequestPin(affectedPost) },
            onSavePost = { onRequestSave(affectedPost) },
            onRequestRetrySend = {
                onRequestRetrySend(
                    affectedPost.clientPostId ?: return@rememberPostActions,
                    affectedPost.content.orEmpty()
                )
            }
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 4.dp),
        state = state
    ) {
        item {
            val postActions = rememberPostActions(post)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostWithBottomSheet(
                    modifier = Modifier
                        .testTag(testTagForPost(post.standalonePostId)),
                    post = post,
                    chatListItem = chatListContextItem ?: ChatListItem.PostItem.ThreadItem.ContextItem.ContextPost(post),
                    postActions = postActions,
                    isMarkedAsDeleteList = isMarkedAsDeleteList,
                    displayHeader = true,
                    joinedItemType = PostItemViewJoinedType.PARENT,
                    clientId = clientId,
                    onClick = {}
                )

                PostActionBar(
                    modifier = Modifier.fillMaxWidth(),
                    post = post,
                    postActions = postActions,
                    repliesCount = post.orderedAnswerPostings.size
                )

                Box {}
            }
        }

        itemsIndexed(
            post.orderedAnswerPostings,
            key = { index, post -> post.clientPostId ?: index }
        ) { index, answerPost ->
            val postActions = rememberPostActions(answerPost)
            val answerChatListItem = answerChatListItemState(answerPost).collectAsState()

            PostWithBottomSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTagForAnswerPost(answerPost.clientPostId)),
                post = answerPost,
                postActions = postActions,
                isMarkedAsDeleteList = isMarkedAsDeleteList,
                chatListItem = answerChatListItem.value ?: ChatListItem.PostItem.ThreadItem.Answer.AnswerPost(answerPost),
                clientId = clientId,
                displayHeader = shouldDisplayHeader(
                    index = index,
                    post = answerPost,
                    postCount = post.orderedAnswerPostings.size,
                    order = DisplayPostOrder.REGULAR,
                    getPost = post.orderedAnswerPostings::get
                ),
                joinedItemType = determinePostItemViewJoinedType(
                    index = index,
                    post = answerPost,
                    postCount = post.orderedAnswerPostings.size,
                    order = DisplayPostOrder.REGULAR,
                    getPost = post.orderedAnswerPostings::get
                ),
                onClick = {}
            )
        }
    }
}
