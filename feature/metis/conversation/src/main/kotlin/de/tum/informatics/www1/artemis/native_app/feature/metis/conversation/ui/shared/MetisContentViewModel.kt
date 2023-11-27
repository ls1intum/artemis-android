package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.holdLatestLoaded
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.FileUploadExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.UnknownExercise
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ConversationWebsocketDto
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.asMetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.subscribeToConversationUpdates
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteHint
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.MetisViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

/**
 * Common viewModel for metis viewModels that display live metis content.
 * Live metis content is content that is being permanently updated by websockets.
 */
abstract class MetisContentViewModel(
    initialMetisContext: MetisContext,
    private val websocketProvider: WebsocketProvider,
    private val metisModificationService: MetisModificationService,
    private val metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val accountDataService: AccountDataService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val conversationService: ConversationService,
    private val replyTextStorageService: ReplyTextStorageService,
    private val courseService: CourseService,
    private val coroutineContext: CoroutineContext
) : MetisViewModel(
    serverConfigurationService,
    accountService,
    accountDataService,
    networkStatusProvider,
    websocketProvider,
    coroutineContext
), InitialReplyTextProvider, ReplyAutoCompleteHintProvider {

    protected val metisContext = MutableStateFlow(initialMetisContext)
    val currentMetisContext: StateFlow<MetisContext> = metisContext

    val hasModerationRights: StateFlow<Boolean> = metisContext.flatMapLatest { metisContext ->
        when (metisContext) {
            is MetisContext.Conversation -> {
                flatMapLatest(
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
            }

            else -> flowOf(false)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    /**
     * Emits true if the data may be outdated. Listens to the connection state of the websocket
     * If a connection was established and is broken, then the data may be corrupted. A reload resets this
     */
    val isDataOutdated: StateFlow<Boolean> = merge(
        onRequestReload,
        metisContext.map { }
    )
        .transformLatest {
            emit(false)
            var wasConnected = false

            websocketProvider.connectionState.collect { connectionState ->
                when (connectionState) {
                    is WebsocketProvider.WebsocketConnectionState.WithSession -> {
                        if (!wasConnected && connectionState.isConnected) {
                            wasConnected = true
                        } else if (wasConnected && !connectionState.isConnected) {
                            emit(true)
                        }
                    }

                    WebsocketProvider.WebsocketConnectionState.Empty -> {
                        wasConnected = false
                    }
                }
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val conversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        metisContext,
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onReloadRequestAndWebsocketReconnect.onStart { emit(Unit) }
    ) { metisContext, serverUrl, authToken, _ ->
        when (metisContext) {
            is MetisContext.Conversation -> retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.getConversation(
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    authToken = authToken,
                    serverUrl = serverUrl
                )
            }

            else -> flowOf(DataState.Failure(RuntimeException("Not a conversation")))
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val latestUpdatedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        metisContext,
        metisContext.flatMapLatest { conversation.holdLatestLoaded() },
        clientId.filterSuccess()
    ) { metisContext, conversationDataState, clientId ->
        websocketProvider.subscribeToConversationUpdates(clientId, metisContext.courseId)
            .filter { it.crudAction == MetisPostAction.UPDATE }
            .map<ConversationWebsocketDto, DataState<Conversation>> { DataState.Success(it.conversation) }
            .onStart { emit(conversationDataState) }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val course: StateFlow<DataState<Course>> = flatMapLatest(
        metisContext,
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { metisContext, serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseService.getCourse(
                metisContext.courseId,
                serverUrl,
                authToken
            ).bind { it.course }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)

    private val conversations: StateFlow<DataState<List<Conversation>>> = flatMapLatest(
        metisContext,
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { metisContext, serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversations(metisContext.courseId, authToken, serverUrl)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)

    override val legalTagChars: List<Char> = listOf('@', '#')

    override val newMessageText: MutableStateFlow<TextFieldValue> =
        MutableStateFlow(TextFieldValue(""))

    init {
        viewModelScope.launch(coroutineContext) {
            // Store the text that was written every 500 millis
            newMessageText
                .debounce(500L)
                .collect { textToStore ->
                    storeNewMessageText(textToStore.text)
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
                createReactionImpl(emojiId, post.asAffectedPost)
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
            context = metisContext.value,
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
            context = metisContext.value,
            reactionId = reactionId,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        ).or(false)

        return if (success) null else MetisModificationFailure.DELETE_REACTION
    }

    protected suspend fun createStandalonePostImpl(post: StandalonePost): MetisModificationFailure? {
        val metisContext = metisContext.value
        val response = metisModificationService.createPost(
            context = metisContext,
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return when (response) {
            is NetworkResponse.Failure -> MetisModificationFailure.CREATE_POST
            is NetworkResponse.Response -> {
                metisStorageService.insertLiveCreatedPost(
                    serverConfigurationService.host.first(),
                    metisContext,
                    response.data
                )

                null
            }
        }
    }

    protected suspend fun createAnswerPostImpl(post: AnswerPost): MetisModificationFailure? {
        val response = metisModificationService.createAnswerPost(
            context = metisContext.value,
            post = post,
            serverUrl = serverConfigurationService.serverUrl.first(),
            authToken = accountService.authToken.first()
        )

        return response.bind<MetisModificationFailure?> { null }
            .or(MetisModificationFailure.CREATE_POST)
    }

    fun deletePost(post: IBasePost): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            metisModificationService.deletePost(
                metisContext.value,
                post.asAffectedPost,
                serverConfigurationService.serverUrl.first(),
                accountService.authToken.first()
            )
                .bind { if (it) null else MetisModificationFailure.DELETE_POST }
                .or(MetisModificationFailure.DELETE_POST)
        }
    }

    fun editPost(post: PostPojo, newText: String): Deferred<MetisModificationFailure?> {
        return viewModelScope.async(coroutineContext) {
            val conversation =
                loadConversation() ?: return@async MetisModificationFailure.UPDATE_POST

            val newPost = StandalonePost(
                post = post.copy(content = newText),
                conversation = conversation
            )

            metisModificationService.updateStandalonePost(
                context = metisContext.value,
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
                context = metisContext.value,
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
            metisContext,
            accountService.authToken,
            serverConfigurationService.serverUrl
        ) { metisContext, authToken, serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService
                    .searchForPotentialCommunicationParticipants(
                        courseId = metisContext.courseId,
                        query = query,
                        includeStudents = true,
                        includeTutors = true,
                        includeInstructors = true,
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
        combine(course, metisContext) { courseDataState, metisContext ->
            courseDataState.bind { course ->
                val exerciseAutoCompleteItems =
                    course
                        .exercises
                        .filter { query in it.title.orEmpty() }
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
                    .filter { query in it.name }
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

    abstract suspend fun getPostId(): Long?

    private val IBasePost.asAffectedPost: MetisModificationService.AffectedPost
        get() = when (this) {
            is AnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is IAnswerPost -> MetisModificationService.AffectedPost.Answer(serverPostId)
            is StandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
            is IStandalonePost -> MetisModificationService.AffectedPost.Standalone(serverPostId)
        }

    protected suspend fun loadConversation(): Conversation? {
        val metisContext = metisContext.value

        if (metisContext !is MetisContext.Conversation) return null

        return conversationService.getConversation(
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId,
            authToken = accountService.authToken.first(),
            serverUrl = serverConfigurationService.serverUrl.first()
        ).orNull()
    }

    fun updateMetisContext(newMetisContext: MetisContext) {
        metisContext.value = newMetisContext

        viewModelScope.launch(coroutineContext) {
            newMessageText.value =
                TextFieldValue(text = retrieveNewMessageText(newMetisContext, getPostId()))
        }
    }

    private suspend fun storeNewMessageText(text: String) {
        when (val metisContext = metisContext.value) {
            is MetisContext.Conversation -> {
                replyTextStorageService.updateStoredReplyText(
                    serverHost = serverConfigurationService.host.first(),
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    postId = getPostId(),
                    text = text
                )
            }

            else -> {}
        }
    }

    protected suspend fun retrieveNewMessageText(
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
}