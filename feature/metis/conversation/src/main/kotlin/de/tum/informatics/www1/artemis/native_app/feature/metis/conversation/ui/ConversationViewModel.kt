package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.holdLatestLoaded
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.isAtLeastTutorInCourse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.FileUploadExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.UnknownExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.BaseImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ConversationChatListUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteHint
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ConversationThreadUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisCrudAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal open class ConversationViewModel(
    val courseId: Long,
    val conversationId: Long,
    initialPostId: StandalonePostId?,
    private val websocketProvider: WebsocketProvider,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    protected val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService,
    private val replyTextStorageService: ReplyTextStorageService,
    private val courseService: CourseService,
    private val createPostService: CreatePostService,
    accountDataService: AccountDataService,
    metisService: MetisService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext
), InitialReplyTextProvider, ReplyAutoCompleteHintProvider {

    private val onRequestSoftReload = onReloadRequestAndWebsocketReconnect

    val metisContext = MetisContext.Conversation(courseId, conversationId)

    private val _postId: MutableStateFlow<StandalonePostId?> = MutableStateFlow(initialPostId)
    val postId: StateFlow<StandalonePostId?> = _postId

    val chatListUseCase = ConversationChatListUseCase(
        viewModelScope = viewModelScope,
        metisService = metisService,
        metisStorageService = metisStorageService,
        metisContext = metisContext,
        onRequestSoftReload = onRequestSoftReload,
        serverConfigurationService = serverConfigurationService,
        accountService = accountService
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

    /**
     * Manages updating from the websocket.
     */
    private val webSocketUpdateUseCase = ConversationWebSocketUpdateUseCase(
        metisService = metisService,
        metisStorageService = metisStorageService
    )

    private val course: StateFlow<DataState<Course>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseService.getCourse(
                metisContext.courseId,
                serverUrl,
                authToken
            ).bind { it.course }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)

    private val hasModerationRights: StateFlow<Boolean> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversation(
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    authToken = authToken,
                    serverUrl = serverUrl
                )
                .bind { it.hasModerationRights }
        }
            .map { it.orElse(false) }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    private val isAtLeastTutorInCourse: StateFlow<Boolean> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        course,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, course, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            accountDataService.getAccountData(
                serverUrl = serverUrl,
                bearerToken = authToken
            )
                .bind { it.isAtLeastTutorInCourse(course = course.orThrow()) }
        }
            .map { it.orElse(false) }
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

    val latestUpdatedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        conversation.holdLatestLoaded(),
        clientId.filterSuccess()
    ) { conversationDataState, clientId ->
        websocketProvider.subscribeToConversationUpdates(clientId, metisContext.courseId)
            .filter { it.crudAction == MetisCrudAction.UPDATE }
            .map<ConversationWebsocketDto, DataState<Conversation>> { DataState.Success(it.conversation) }
            .onStart { emit(conversationDataState) }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val conversations: StateFlow<DataState<List<Conversation>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversations(metisContext.courseId, authToken, serverUrl)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)

    private val isAbleToPin: StateFlow<Boolean> = conversation
        .map { conversation -> conversation.bind {
            when (it) {
                // Group Chat: Only Creator can pin
                is GroupChat -> it.isCreator
                // Channel: Only Moderators can pin
                is ChannelChat -> it.hasModerationRights
                else -> true
            }
        }.orElse(false) }
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

    override val legalTagChars: List<Char> = listOf('@', '#')

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
            combine(serverConfigurationService.host, clientId.filterSuccess()) { host, clientId -> host to clientId }
                .collect { (host, clientId) ->
                    webSocketUpdateUseCase.updatePosts(
                        host = host,
                        context = metisContext,
                        clientId = clientId
                    )
                }
        }
    }

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
        parentPost: PostPojo,
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

    fun deletePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            metisModificationService.deletePost(
                metisContext,
                post.getAsAffectedPost() ?: return@async MetisModificationFailure.DELETE_POST,
                serverConfigurationService.serverUrl.first(),
                accountService.authToken.first()
            )
                .bind { if (it) null else MetisModificationFailure.DELETE_POST }
                .or(MetisModificationFailure.DELETE_POST)
        }
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
        parentPost: PostPojo,
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

    override fun produceAutoCompleteHints(
        tagChar: Char,
        query: String
    ): Flow<DataState<List<AutoCompleteCategory>>> = when (tagChar) {
        '@' -> {
            produceUserMentionAutoCompleteHints(query)
        }

        '#' -> {
            combine(
                produceExerciseAndLectureAutoCompleteHints(query),
                produceConversationAutoCompleteHints(query)
            ) { exerciseAndLectureHints, conversationHints ->
                (exerciseAndLectureHints join conversationHints)
                    .bind { (a, b) -> a + b }
            }
        }

        else -> flowOf(DataState.Success(emptyList()))
    }
        // Only display categories with at least 1 hint.
        .map { autoCompleteCategoriesDataState ->
            autoCompleteCategoriesDataState.bind { autoCompleteCategories ->
                autoCompleteCategories
                    .filter { it.items.isNotEmpty() }
            }
        }

    private fun produceUserMentionAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteCategory>>> =
        flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl
        ) { authToken, serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService
                    .searchForCourseMembers(
                        courseId = metisContext.courseId,
                        query = query,
                        authToken = authToken,
                        serverUrl = serverUrl
                    )
                    .bind { users ->
                        AutoCompleteCategory(
                            name = R.string.markdown_textfield_autocomplete_category_users,
                            items = users.map {
                                AutoCompleteHint(
                                    it.name.orEmpty(),
                                    replacementText = "[user]${it.name}(${it.username})[/user]",
                                    id = it.username.orEmpty()
                                )
                            }
                        )
                            .let(::listOf)
                    }
            }
        }

    private fun produceExerciseAndLectureAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteCategory>>> =
        course.map { courseDataState ->
            courseDataState.bind { course ->
                val exerciseAutoCompleteItems =
                    course
                        .exercises
                        .filter { it.title.orEmpty().contains(query, ignoreCase = true) }
                        .mapNotNull { exercise ->
                            val exerciseTag = when (exercise) {
                                is FileUploadExercise -> "file-upload"
                                is ModelingExercise -> "modeling"
                                is ProgrammingExercise -> "programming"
                                is QuizExercise -> "quiz"
                                is TextExercise -> "text"
                                is UnknownExercise -> return@mapNotNull null
                            }

                            val exerciseTitle = exercise.title ?: return@mapNotNull null

                            AutoCompleteHint(
                                hint = exerciseTitle,
                                replacementText = "[$exerciseTag]${exercise.title}(/courses/${metisContext.courseId}/exercises/${exercise.id})[/$exerciseTag]",
                                id = "Exercise:${exercise.id ?: return@mapNotNull null}"
                            )
                        }

                val lectureAutoCompleteItems =
                    course
                        .lectures
                        .filter { query in it.title }
                        .mapNotNull { lecture ->
                            AutoCompleteHint(
                                hint = lecture.title,
                                replacementText = "[lecture]${lecture.title}(/courses/${metisContext.courseId}/lectures/${lecture.id})[/lecture]",
                                id = "Lecture:${lecture.id ?: return@mapNotNull null}"
                            )
                        }

                listOf(
                    AutoCompleteCategory(
                        name = R.string.markdown_textfield_autocomplete_category_exercises,
                        items = exerciseAutoCompleteItems
                    ),
                    AutoCompleteCategory(
                        name = R.string.markdown_textfield_autocomplete_category_lectures,
                        items = lectureAutoCompleteItems
                    )
                )
            }
        }

    private fun produceConversationAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteCategory>>> =
        conversations.map { conversationsDataState ->
            conversationsDataState.bind { conversations ->
                val conversationAutoCompleteItems = conversations
                    .filterIsInstance<ChannelChat>()
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map { channel ->
                        AutoCompleteHint(
                            hint = channel.name,
                            replacementText = "[channel]${channel.name}(${channel.id})[/channel]",
                            id = "Channel:${channel.id}"
                        )
                    }

                listOf(
                    AutoCompleteCategory(
                        name = R.string.markdown_textfield_autocomplete_category_channels,
                        items = conversationAutoCompleteItems
                    )
                )
            }
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

            val post = metisStorageService.getStandalonePost(clientSidePostId).first() ?: return@launch

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

    fun createMarkdownImageLoader(context: Context): Deferred<ImageLoader> {
        return viewModelScope.async(coroutineContext) {
            val imageProvider = BaseImageProviderImpl()
            val authorizationToken = accountService.authToken.first()
            imageProvider.createImageLoader(context, authorizationToken)
        }
    }
}