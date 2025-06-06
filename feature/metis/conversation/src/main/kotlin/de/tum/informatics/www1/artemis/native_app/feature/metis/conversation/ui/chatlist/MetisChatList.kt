package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.DisplayPostOrder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.PostWithBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.determinePostItemViewJoinedType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.rememberPostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.shouldDisplayHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MetisReplyHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MetisReplyHandlerInputActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.util.rememberDerivedConversationName
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.PagingStateError
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import kotlinx.coroutines.flow.StateFlow
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
    state: LazyListState = rememberLazyListState(),
    isReplyEnabled: Boolean = true,
    onClickViewPost: (StandalonePostId) -> Unit,
) {
    ReportVisibleMetisContext(remember(viewModel.metisContext) { VisiblePostList(viewModel.metisContext) })

    val clientId: Long by viewModel.clientIdOrDefault.collectAsState()
    val postActionFlags by viewModel.postActionFlags.collectAsState()
    val forwardMessageUseCase = viewModel.forwardMessageUseCase

    val serverUrl by viewModel.serverUrl.collectAsState()

    val bottomItem: PostPojo? by viewModel.chatListUseCase.bottomPost.collectAsState()

    val conversationDataState by viewModel.latestUpdatedConversation.collectAsState()

    val conversationName by rememberDerivedConversationName(conversationDataState)

    val context = LocalContext.current

    ProvideMarkwon {
        MetisChatList(
            modifier = modifier,
            initialReplyTextProvider = viewModel,
            posts = posts.asPostsDataState(),
            clientId = clientId,
            postActionFlags = postActionFlags,
            serverUrl = serverUrl,
            courseId = viewModel.courseId,
            state = state,
            isMarkedAsDeleteList = viewModel.isMarkedAsDeleteList,
            bottomItem = bottomItem,
            forwardMessageUseCase = forwardMessageUseCase,
            isReplyEnabled = isReplyEnabled,
            actions = MetisReplyHandlerInputActions(
                create = viewModel::createPost,
                edit = viewModel::editPost,
                delete = viewModel::deletePost,
                react = viewModel::createOrDeleteReaction,
                save = viewModel::toggleSavePost,
                pin = viewModel::togglePinPost,
                resolve = null
            ),
            onClickViewPost = onClickViewPost,
            onRequestRetrySend = viewModel::retryCreatePost,
            onUndoDeletePost = viewModel::undoDeletePost,
            conversationName = conversationName,
            generateLinkPreviews = viewModel::generateLinkPreviews,
            onRemoveLinkPreview = viewModel::removeLinkPreview,
            onFileSelected = { uri ->
                viewModel.onFileSelected(uri, context)
            }
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
    forwardMessageUseCase: ForwardMessageUseCase,
    serverUrl: String,
    courseId: Long,
    state: LazyListState,
    emojiService: EmojiService = koinInject(),
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    isReplyEnabled: Boolean,
    actions: MetisReplyHandlerInputActions<IStandalonePost>,
    onClickViewPost: (StandalonePostId) -> Unit,
    onUndoDeletePost: (IStandalonePost) -> Unit,
    generateLinkPreviews: (String) -> StateFlow<List<LinkPreview>>,
    onRemoveLinkPreview: (LinkPreview, IBasePost, IStandalonePost?) -> Unit,
    onRequestRetrySend: (StandalonePostId) -> Unit,
    conversationName: String,
    onFileSelected: (Uri) -> Unit
) {
    MetisReplyHandler(
        initialReplyTextProvider = initialReplyTextProvider,
        actions = actions,
    ) { replyMode, actionsDelegate, updateFailureStateDelegate ->
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
                        EmptyListHint(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                                .align(Alignment.Center),
                            hint = stringResource(id = R.string.metis_post_list_empty),
                            secondaryHint = stringResource(id = R.string.metis_post_list_empty_secondary),
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                        )
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
                            state = state,
                            posts = posts,
                            clientId = clientId,
                            isMarkedAsDeleteList = isMarkedAsDeleteList,
                            onClickViewPost = onClickViewPost,
                            postActionFlags = postActionFlags,
                            forwardMessageUseCase = forwardMessageUseCase,
                            onRequestEdit = actionsDelegate.edit,
                            onRequestDelete = actionsDelegate.delete,
                            onRequestUndoDelete = onUndoDeletePost,
                            onRequestPin = actionsDelegate.pin,
                            onRequestSave = actionsDelegate.save,
                            onRequestReactWithEmoji = actionsDelegate.react,
                            onRequestRetrySend = onRequestRetrySend,
                            generateLinkPreviews = generateLinkPreviews,
                            onRemoveLinkPreview = onRemoveLinkPreview
                        )
                    }
                }
            }

            if (isReplyEnabled) {
                ReplyTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    replyMode = replyMode,
                    updateFailureState = updateFailureStateDelegate,
                    conversationName = conversationName,
                    onFileSelected = onFileSelected,
                    emojiService = emojiService
                )
            }
        }
    }
}

@Composable
private fun ChatList(
    modifier: Modifier,
    state: LazyListState,
    posts: PostsDataState.Loaded,
    postActionFlags: PostActionFlags,
    forwardMessageUseCase: ForwardMessageUseCase,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    clientId: Long,
    displayUnreadIndicator: Boolean = false,        // See https://github.com/ls1intum/artemis-android/pull/375#issuecomment-2656030353 and https://github.com/ls1intum/artemis-android/issues/394#issuecomment-2911878771
    onClickViewPost: (StandalonePostId) -> Unit,
    onRequestEdit: (IStandalonePost) -> Unit,
    onRequestDelete: (IStandalonePost) -> Unit,
    onRequestUndoDelete: (IStandalonePost) -> Unit,
    onRequestPin: (IStandalonePost) -> Unit,
    onRequestSave: (IStandalonePost) -> Unit,
    onRequestReactWithEmoji: (IStandalonePost, emojiId: String, create: Boolean) -> Unit,
    onRequestRetrySend: (StandalonePostId) -> Unit,
    generateLinkPreviews: (String) -> StateFlow<List<LinkPreview>>,
    onRemoveLinkPreview: (LinkPreview, IBasePost, IStandalonePost?) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(bottom = 4.dp),
        reverseLayout = true
    ) {
        items(
            count = posts.itemCount,
            key = posts::getItemKey
        ) { index ->
            @Suppress("KotlinConstantConditions")
            when (val chatListItem = posts[index]) {
                is ChatListItem.UnreadIndicator -> {
                    if (displayUnreadIndicator) {
                        UnreadPostsIndicator()
                    }
                }

                is ChatListItem.DateDivider -> {
                    DateDivider(
                        modifier = Modifier
                            .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                            .fillMaxWidth(),
                        date = chatListItem.localDate
                    )
                }

                is ChatListItem.PostItem? -> {
                    if (chatListItem !is ChatListItem.PostItem.IndexedItem) return@items
                    val post = chatListItem.post as IStandalonePost
                    val linkPreviews by remember(post.content) {
                        generateLinkPreviews(post.content.orEmpty())
                    }.collectAsState()
                    var displayForwardPostBottomSheet by remember(post) { mutableStateOf(false) }

                    val postActions = rememberPostActions(
                        chatListItem = chatListItem,
                        postActionFlags = postActionFlags,
                        clientId = clientId,
                        onRequestEdit = { onRequestEdit(post) },
                        onRequestDelete = {
                            onRequestDelete(post)
                        },
                        onRequestUndoDelete = {
                            onRequestUndoDelete(post)
                        },
                        onClickReaction = { id, create ->
                            onRequestReactWithEmoji(post, id, create)
                        },
                        onReplyInThread = {
                            onClickViewPost(post.standalonePostId ?: return@rememberPostActions)
                        },
                        onResolvePost = null,
                        onPinPost = { onRequestPin(post) },
                        onForwardPost = { displayForwardPostBottomSheet = true },
                        onSavePost = { onRequestSave(post) },
                        onRequestRetrySend = {
                            onRequestRetrySend(
                                post.standalonePostId ?: return@rememberPostActions
                            )
                        }
                    )

                    PostWithBottomSheet(
                        modifier = Modifier
                            .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                            .fillMaxWidth()
                            .testTag(testTagForPost(post.standalonePostId)),
                        post = post,
                        clientId = clientId,
                        isMarkedAsDeleteList = isMarkedAsDeleteList,
                        chatListItem = chatListItem,
                        postActions = postActions,
                        linkPreviews = linkPreviews,
                        forwardMessageUseCase = forwardMessageUseCase,
                        displayForwardBottomSheet = displayForwardPostBottomSheet,
                        displayHeader = shouldDisplayHeader(
                            index = index,
                            post = post,
                            postCount = posts.itemCount,
                            order = DisplayPostOrder.REVERSED,
                            getPost = { getPostIndex ->
                                when (val entry = posts.peek(getPostIndex)) {
                                    is ChatListItem.PostItem.IndexedItem -> entry.post
                                    else -> null
                                }
                            }
                        ),
                        joinedItemType = determinePostItemViewJoinedType(
                                index = index,
                                post = post,
                                postCount = posts.itemCount,
                                order = DisplayPostOrder.REVERSED,
                                getPost = { getPostIndex ->
                                    when (val entry = posts.peek(getPostIndex)) {
                                        is ChatListItem.PostItem.IndexedItem -> entry.post
                                        else -> null
                                    }
                                }
                            ),
                        onRemoveLinkPreview = { linkPreview ->
                            onRemoveLinkPreview(linkPreview, post, null)
                        },
                        onClick = {
                            val standalonePostId = post.standalonePostId

                            if (post.serverPostId != null && standalonePostId != null) {
                                onClickViewPost(standalonePostId)
                            }
                        },
                        dismissForwardBottomSheet = { displayForwardPostBottomSheet = false }
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
private fun UnreadPostsIndicator(
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(color)
        )

        Text(
            modifier = Modifier
                .padding(end = 32.dp)
                .align(Alignment.CenterEnd)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.small
                )
                .padding(4.dp),
            text = stringResource(R.string.unread_post_indicator_text),
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
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

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dateAsString,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}