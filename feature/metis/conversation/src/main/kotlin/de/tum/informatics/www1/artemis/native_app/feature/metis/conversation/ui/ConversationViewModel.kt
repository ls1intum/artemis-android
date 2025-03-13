package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.holdLatestLoaded
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.PostArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.FileValidationConstants.isImage
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.Link
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ConversationChatListUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.LinkPreviewUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompletionUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ConversationThreadUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisCrudAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToConversationUpdates
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.MetisViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// A buffer is added to the delay to account for additional delays
// NOTE: This is only for the viewModel. Check PostItem.kt for the visible delay in the UI.
private const val undoDeleteDelayFromUi = 6000L
private const val bufferDelay = 1000L
private const val undoDeleteDelay = undoDeleteDelayFromUi + bufferDelay

internal open class ConversationViewModel(
    val courseId: Long,
    val conversationId: Long,
    initialPostId: StandalonePostId?,
    private val websocketProvider: WebsocketProvider,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val savedPostService: SavedPostService,
    protected val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService,
    private val replyTextStorageService: ReplyTextStorageService,
    courseService: CourseService,
    private val createPostService: CreatePostService,
    faqRepository: FaqRepository,
    accountDataService: AccountDataService,
    private val metisService: MetisService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisViewModel(
    courseService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext,
    courseId
), InitialReplyTextProvider {

    private var currentlySavingPost = false

    private val onRequestSoftReload = onReloadRequestAndWebsocketReconnect

    val metisContext = MetisContext.Conversation(courseId, conversationId)

    private val _postId: MutableStateFlow<StandalonePostId?> = MutableStateFlow(initialPostId)
    val postId: StateFlow<StandalonePostId?> = _postId

    private val deleteJobs = mutableMapOf<IBasePost, Job>()
    val isMarkedAsDeleteList = mutableStateListOf<IBasePost>()

    val conversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onReloadRequestAndWebsocketReconnect.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService.getConversation(
                courseId = metisContext.courseId,
                conversationId = metisContext.conversationId,
                authToken = authToken,
                serverUrl = serverUrl
            )
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val chatListUseCase = ConversationChatListUseCase(
        viewModelScope = viewModelScope,
        metisService = metisService,
        metisStorageService = metisStorageService,
        metisContext = metisContext,
        onRequestSoftReload = onRequestSoftReload,
        serverConfigurationService = serverConfigurationService,
        accountService = accountService,
        conversation = conversation,
        coroutineContext = coroutineContext
    )

    val threadUseCase = ConversationThreadUseCase(
        metisContext = metisContext,
        postId = postId.filterNotNull(),
        onRequestSoftReload = onRequestSoftReload,
        viewModelScope = viewModelScope,
        metisStorageService = metisStorageService,
        metisService = metisService,
        networkStatusProvider = networkStatusProvider,
        serverConfigurationService = serverConfigurationService,
        accountService = accountService,
        coroutineContext = coroutineContext
    )

    val autoCompletionUseCase = AutoCompletionUseCase(
        courseId = courseId,
        metisContext = metisContext,
        viewModelScope = viewModelScope,
        conversationService = conversationService,
        faqRepository = faqRepository,
        accountService = accountService,
        serverConfigurationService = serverConfigurationService,
        networkStatusProvider = networkStatusProvider,
        course = course,
        coroutineContext = coroutineContext
    )

    /**
     * Manages updating from the websocket.
     */
    private val webSocketUpdateUseCase = ConversationWebSocketUpdateUseCase(
        websocketProvider = websocketProvider,
        metisStorageService = metisStorageService
    )

    private val hasModerationRights: StateFlow<Boolean> = conversation.map {
        it.bind { conversation -> conversation.hasModerationRights }
            .orElse(false)
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val conversationDataStatus: StateFlow<DataStatus> = combine(
        websocketProvider.isConnected,
        chatListUseCase.dataStatus,
        postId.flatMapLatest { postId ->
            if (postId != null) threadUseCase.dataStatus else flowOf(DataStatus.UpToDate)
        }
    ) { websocketConnected, chatListStatus, threadStatus ->
        when {
            !websocketConnected -> DataStatus.Outdated
            else -> minOf(chatListStatus, threadStatus)
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataStatus.Outdated)


    val latestUpdatedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        conversation.holdLatestLoaded(),
        clientId.filterSuccess()
    ) { conversationDataState, clientId ->
        websocketProvider.subscribeToConversationUpdates(clientId, metisContext.courseId)
            .filter { it.action == MetisCrudAction.UPDATE }
            .map<ConversationWebsocketDto, DataState<Conversation>> { DataState.Success(it.conversation) }
            .onStart { emit(conversationDataState) }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val isAbleToPin: StateFlow<Boolean> = conversation
        .map { conversation ->
            conversation.bind {
                when (it) {
                    // Group Chat: Only Creator can pin
                    is GroupChat -> it.isCreator
                    // Channel: Only Moderators can pin
                    is ChannelChat -> it.hasModerationRights
                    else -> true
                }
            }.orElse(false)
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val postActionFlags: StateFlow<PostActionFlags> = combine(
        isAbleToPin,
        hasModerationRights,
        isAtLeastTutorInCourse
    ) { isAbleToPin, hasModerationRights, isAtLeastTutorInCourse ->
        PostActionFlags(
            isAbleToPin = isAbleToPin,
            hasModerationRights = hasModerationRights,
            isAtLeastTutorInCourse = isAtLeastTutorInCourse
        )
    }.stateIn(
        scope = viewModelScope + coroutineContext,
        started = SharingStarted.Eagerly,
        initialValue = PostActionFlags(
            isAbleToPin = false,
            hasModerationRights = false,
            isAtLeastTutorInCourse = false
        )
    )

    override val newMessageText: MutableStateFlow<TextFieldValue> =
        MutableStateFlow(TextFieldValue(""))

    val serverUrl = serverUrlStateFlow(serverConfigurationService)

    init {
        viewModelScope.launch(coroutineContext) {
            // Store the text that was written every 500 millis
            newMessageText
                .debounce(500L)
                .collect { textToStore ->
                    storeNewMessageText(textToStore.text)
                }
        }

        // Receive websocket updates and store them in the db.
        viewModelScope.launch(coroutineContext) {
            combine(
                serverConfigurationService.host,
                clientId.filterSuccess()
            ) { host, clientId -> host to clientId }
                .collect { (host, clientId) ->
                    webSocketUpdateUseCase.updatePosts(
                        host = host,
                        context = metisContext,
                        clientId = clientId
                    )
                }
        }
    }

    var onCloseThread: (() -> Unit)? = null

    /**
     * Handles a reaction click. If the client has already reacted, it deletes the reaction.
     * Otherwise it creates a reaction with the same emoji id.
     */
    fun createOrDeleteReaction(
        post: IBasePost,
        emojiId: String,
        create: Boolean
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            if (create) {
                createReactionImpl(
                    emojiId,
                    post.getAsAffectedPost()
                        ?: return@async MetisModificationFailure.DELETE_REACTION
                )
            } else {
                val clientId = clientId.value.orNull()
                    ?: return@async MetisModificationFailure.DELETE_REACTION

                val exitingReactionId = post
                    .reactions
                    .orEmpty()
                    .filter { it.emojiId == emojiId }
                    .firstOrNull { it.creatorId == clientId }
                    ?.id
                    ?: return@async MetisModificationFailure.DELETE_REACTION

                deleteReactionImpl(exitingReactionId)
            }
        }
    }

    private suspend fun createReactionImpl(
        emojiId: String,
        post: MetisModificationService.AffectedPost
    ): MetisModificationFailure? {
        val networkResponse: NetworkResponse<Reaction> = metisModificationService.createReaction(
            context = metisContext,
            post = post,
            emojiId = emojiId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return networkResponse.bind<MetisModificationFailure?> { null }
            .or(MetisModificationFailure.CREATE_REACTION)
    }

    private suspend fun deleteReactionImpl(reactionId: Long): MetisModificationFailure? {
        val success = metisModificationService.deleteReaction(
            context = metisContext,
            reactionId = reactionId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        ).or(false)

        return if (success) null else MetisModificationFailure.DELETE_REACTION
    }

    /**
     * Handles a click on resolve or does not resolve post.
     * It updates the post accordingly.
     */
    fun toggleResolvePost(
        parentPost: IStandalonePost,
        post: AnswerPostPojo
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val resolved = !post.resolvesPost
            val serializedParentPost = StandalonePost(parentPost, conversation)
            val newPost = AnswerPost(post, serializedParentPost).copy(resolvesPost = resolved)

            metisModificationService.updateAnswerPost(
                context = metisContext,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun togglePinPost(post: IStandalonePost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val newDisplayPriority = if (post.displayPriority == DisplayPriority.PINNED) {
                DisplayPriority.NONE
            } else {
                DisplayPriority.PINNED
            }

            val newPost = when (post) {
                is StandalonePost -> post.copy(displayPriority = newDisplayPriority)
                is PostPojo -> StandalonePost(
                    post = post.copy(displayPriority = newDisplayPriority),
                    conversation = conversation
                )

                else -> throw IllegalArgumentException()
            }

            metisModificationService.updatePostDisplayPriority(
                context = metisContext,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first(),
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun toggleSavePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        // TODO: this is a quick fix to prevent multiple save requests.
        //      https://github.com/ls1intum/artemis-android/issues/307
        if (currentlySavingPost) return CompletableDeferred(null)
        currentlySavingPost = true

        return viewModelScope.async(coroutineContext) {
            val response = if (post.isSaved == true) {
                savedPostService.deleteSavedPost(
                    post = post,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
            } else {
                savedPostService.savePost(
                    post = post,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
            }

            currentlySavingPost = false
            response.bind { requestReload() }   // Currently changing save status does not trigger a websocket update
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)

        }
    }

    fun deletePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        isMarkedAsDeleteList.add(post)
        deleteJobs[post]?.cancel()

        val deleteJob = viewModelScope.async(coroutineContext) {
            try {
                delay(undoDeleteDelay)

                if (isMarkedAsDeleteList.contains(post)) {
                    metisModificationService.deletePost(
                        metisContext,
                        post.getAsAffectedPost() ?: return@async MetisModificationFailure.DELETE_POST,
                        serverConfigurationService.serverUrl.first(),
                        accountService.authToken.first()
                    )
                        .bind { if (it) null else MetisModificationFailure.DELETE_POST }
                        .or(MetisModificationFailure.DELETE_POST)
                        .also { if (it != MetisModificationFailure.DELETE_POST && post is IStandalonePost) onCloseThread?.invoke() }
                } else {
                    null
                }
            } finally {
                deleteJobs.remove(post)
                isMarkedAsDeleteList.remove(post)
            }
        }
        deleteJobs[post] = deleteJob

        return deleteJob
    }

    fun undoDeletePost(post: IBasePost) {
        deleteJobs[post]?.cancel()
        deleteJobs.remove(post)
        isMarkedAsDeleteList.remove(post)
    }

    fun editPost(post: IStandalonePost, newText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val newPost = when (post) {
                is StandalonePost -> post.copy(content = newText)
                is PostPojo -> StandalonePost(
                    post = post.copy(content = newText),
                    conversation = conversation
                )

                else -> throw IllegalArgumentException()
            }

            metisModificationService.updateStandalonePost(
                context = metisContext,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun editAnswerPost(
        parentPost: IStandalonePost,
        post: AnswerPostPojo,
        newText: String
    ): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val serializedParentPost = StandalonePost(parentPost, conversation)
            val newPost = AnswerPost(post, serializedParentPost).copy(content = newText)

            metisModificationService.updateAnswerPost(
                context = metisContext,
                post = newPost,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .asMetisModificationFailure(MetisModificationFailure.UPDATE_POST)
        }
    }

    fun removeLinkPreview(
        linkPreview: LinkPreview,
        post: IBasePost,
        parentPost: IStandalonePost?
    ): Deferred<MetisModificationFailure?> {
        val newContent = LinkPreviewUtil.removeLinkPreview(post.content.orEmpty(), linkPreview.url)
        return when (post) {
            is IStandalonePost -> editPost(post, newContent)
            is AnswerPostPojo -> {
                parentPost?.let { editAnswerPost(it, post, newContent) } ?: throw IllegalArgumentException()
            }
            else -> throw IllegalArgumentException()
        }
    }

    fun generateLinkPreviews(postContent: String): StateFlow<List<LinkPreview>> =
        combine(
            accountService.authToken,
            serverConfigurationService.serverUrl
        ) { authToken, serverUrl ->
            val links = LinkPreviewUtil.generatePreviewableLinks(postContent)
                .filter { it.isLinkPreviewRemoved != true }
                .take(LinkPreviewUtil.MAX_LINK_PREVIEWS_PER_MESSAGE)

            val previews = links.map { link ->
                viewModelScope.async {
                    fetchPreview(link, authToken, serverUrl)
                }
            }.awaitAll().filterNotNull()
            previews
        }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    private suspend fun fetchPreview(
        link: Link,
        authToken: String,
        serverUrl: String
    ): LinkPreview? {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            metisService.fetchLinkPreview(
                url = link.value,
                authToken = authToken,
                serverUrl = serverUrl
            ).bind { preview ->
                preview?.takeIf { it.isValid() }?.apply {
                    shouldPreviewBeShown = true
                }
            }
        }.filterSuccess().firstOrNull()
    }

    init {
        // Reload the newMessageText whenever postId changes
        viewModelScope.launch(coroutineContext) {
            postId.collect { newPostId ->
                newMessageText.value = TextFieldValue(
                    retrieveNewMessageText(
                        metisContext,
                        getPostId(newPostId)
                    )
                )
            }
        }
    }

    /**
     * Emits to onRequestReload. If the websocket is currently not connected, requests a reconnect to the websocket
     */
    override fun requestReload() {
        super.requestReload()

        viewModelScope.launch(coroutineContext) {
            if (!websocketProvider.isConnected.first()) {
                websocketProvider.requestTryReconnect()
            }
        }
    }

    override fun updateInitialReplyText(text: TextFieldValue) {
        newMessageText.value = text
    }

    private fun IBasePost.getAsAffectedPost(): MetisModificationService.AffectedPost? {
        val sPostId = serverPostId ?: return null

        return when (this) {
            is AnswerPost -> MetisModificationService.AffectedPost.Answer(sPostId)
            is IAnswerPost -> MetisModificationService.AffectedPost.Answer(sPostId)
            is StandalonePost -> MetisModificationService.AffectedPost.Standalone(sPostId)
            is IStandalonePost -> MetisModificationService.AffectedPost.Standalone(sPostId)
            is SavedPost -> null
            is ISavedPost -> null
        }
    }

    private suspend fun loadConversation(): Conversation? {
        return conversationService.getConversation(
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId,
            authToken = accountService.authToken.first(),
            serverUrl = serverConfigurationService.serverUrl.first()
        ).orNull()
    }

    private suspend fun storeNewMessageText(text: String) {
        replyTextStorageService.updateStoredReplyText(
            serverHost = serverConfigurationService.host.first(),
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId,
            postId = getPostId(),
            text = text
        )
    }

    private suspend fun retrieveNewMessageText(
        metisContext: MetisContext,
        postId: Long?
    ): String {
        return when (metisContext) {
            is MetisContext.Conversation -> {
                replyTextStorageService.getStoredReplyText(
                    serverHost = serverConfigurationService.host.first(),
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    postId = postId
                )
            }

            else -> ""
        }
    }

    private suspend fun getPostId(postId: StandalonePostId? = _postId.value): Long? =
        when (postId) {
            is StandalonePostId.ClientSideId -> metisStorageService.getStandalonePost(postId.clientSideId)
                .filterNotNull().first().serverPostId

            is StandalonePostId.ServerSideId -> postId.serverSidePostId
            null -> null
        }

    fun createPost(): Deferred<MetisModificationFailure?> {
        createPostService.createPost(courseId, conversationId, newMessageText.value.text)

        return CompletableDeferred(value = null)
    }

    fun retryCreatePost(standalonePostId: StandalonePostId) {
        viewModelScope.launch(coroutineContext) {
            val clientSidePostId = when (standalonePostId) {
                is StandalonePostId.ClientSideId -> standalonePostId.clientSideId
                is StandalonePostId.ServerSideId -> metisStorageService.getClientSidePostId(
                    serverConfigurationService.host.first(),
                    standalonePostId.serverSidePostId,
                    postingType = BasePostingEntity.PostingType.STANDALONE
                )
            } ?: return@launch

            val post =
                metisStorageService.getStandalonePost(clientSidePostId).first() ?: return@launch

            createPostService.retryCreatePost(
                courseId = courseId,
                conversationId = conversationId,
                clientSidePostId = clientSidePostId,
                content = post.content
            )
        }
    }

    fun createReply(): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val serverSideParentPostId =
                getPostId() ?: return@async MetisModificationFailure.CREATE_POST

            createPostService.createAnswerPost(
                courseId,
                conversationId,
                serverSideParentPostId,
                newMessageText.value.text
            )

            null
        }
    }

    fun retryCreateReply(clientPostId: String, content: String) {
        viewModelScope.launch(coroutineContext) {
            createPostService.retryCreatePost(
                courseId = courseId,
                conversationId = conversationId,
                clientSidePostId = clientPostId,
                content = content
            )
        }
    }

    fun updateOpenedThread(newPostId: StandalonePostId?) {
        _postId.value = newPostId
    }

    fun onFileSelected(uri: Uri, context: Context) {
        val fileName = resolveFileName(context, uri)
        uploadFileOrImage(context = context, fileUri = uri, fileName = fileName)
    }

    private fun uploadFileOrImage(context: Context, fileUri: Uri, fileName: String) {
        viewModelScope.launch(coroutineContext) {
            try {
                val fileBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        val fileSize = inputStream.available()

                        val maxFileSize = 5 * 1024 * 1024
                        if (fileSize > maxFileSize) {
                            throw IllegalArgumentException(
                                getString(
                                    context,
                                    R.string.conversation_vm_file_size_exceed
                                )
                            )
                        }
                        inputStream.readBytes()
                    }
                } ?: throw IllegalArgumentException(
                    getString(
                        context,
                        R.string.conversation_vm_file_upload_failed
                    )
                )

                val response = metisModificationService.uploadFileOrImage(
                    context = metisContext,
                    fileBytes = fileBytes,
                    fileName = fileName,
                    serverUrl = serverConfigurationService.serverUrl.first(),
                    authToken = accountService.authToken.first()
                )

                when (response) {
                    is NetworkResponse.Response -> {
                        val fileUploadResponse = response.data
                        val filePath = fileUploadResponse.path
                            ?.let { if (it.startsWith("/")) it.substring(1) else it }
                            ?: throw IllegalArgumentException(
                                getString(
                                    context,
                                    R.string.conversation_vm_file_upload_failed
                                )
                            )

                        val currentText = newMessageText.value.text

                        val transformer = PostArtemisMarkdownTransformer(
                            serverUrl = serverConfigurationService.serverUrl.first(),
                            courseId = metisContext.courseId
                        )

                        val markdown: String = transformer.transformFileUploadMessageMarkdown(
                            isImage = isImage(fileName),
                            fileName = fileName,
                            filePath = filePath
                        )
                        val updatedText = "$currentText\n$markdown\n"
                        newMessageText.value = TextFieldValue(updatedText)
                    }

                    else -> {
                        throw IllegalArgumentException(
                            getString(
                                context,
                                R.string.conversation_vm_file_upload_failed
                            ))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun resolveFileName(context: Context, uri: Uri): String {
        val resolver: ContentResolver = context.contentResolver
        return resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: uri.lastPathSegment.orEmpty()
    }

}