package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
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
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.FaqDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIconId
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common.getChannelIconImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AutoCompletionUseCase(
    private val courseId: Long,
    private val metisContext: MetisContext,
    viewModelScope: CoroutineScope,
    private val conversationService: ConversationService,
    private val faqRepository: FaqRepository,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val course: StateFlow<DataState<Course>>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReplyAutoCompleteHintProvider {

    private val conversations: StateFlow<DataState<List<Conversation>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
    ) { serverUrl, authToken, ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversations(metisContext.courseId, authToken, serverUrl)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily)


    override val legalTagChars: List<Char> = listOf('@', '#')

    override var isFaqEnabled: Boolean = false

    init {
        viewModelScope.launch(coroutineContext) {
            course.collectLatest {
                if (it is DataState.Success) {
                    isFaqEnabled = it.data.faqEnabled
                }
            }
        }
    }

    override fun produceAutoCompleteHints(
        tagChar: Char,
        query: String
    ): Flow<DataState<List<AutoCompleteHintCollection>>> = when (tagChar) {
        '@' -> produceUserMentionAutoCompleteHints(query)
        '#' -> combine(
            produceExerciseAndLectureAutoCompleteHints(query),
            produceConversationAutoCompleteHints(query),
            produceFaqAutoCompletionHints(query)
        ) { exerciseAndLectureHints, conversationHints, faqHints ->
            exerciseAndLectureHints.join(conversationHints, faqHints)
                .bind { (a, b, c) -> a + b + c }
        }
        else -> flowOf(DataState.Success(emptyList()))
    }.map { autoCompleteCategoriesDataState ->
        autoCompleteCategoriesDataState.bind { autoCompleteCategories ->
            autoCompleteCategories.filter { it.items.isNotEmpty() }
        }
    }

    private fun produceUserMentionAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteHintCollection>>> =
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
                        AutoCompleteHintCollection(
                            type = AutoCompleteType.USERS,
                            items = users.map {
                                AutoCompleteHint(
                                    it.name.orEmpty(),
                                    replacementText = "[user]${it.name}(${it.username})[/user]",
                                    id = it.username.orEmpty()
                                )
                            }
                        ).let(::listOf)
                    }
            }
        }

    private fun produceExerciseAndLectureAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteHintCollection>>> =
        course.map { courseDataState ->
            courseDataState.bind { course ->
                val exerciseAutoCompleteItems = course.exercises
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
                        val exerciseId = exercise.id ?: return@mapNotNull null
                        val link = ExerciseDeeplinks.ToExercise.markdownLink(courseId, exerciseId)

                        AutoCompleteHint(
                            hint = exerciseTitle,
                            replacementText = "[$exerciseTag]${exercise.title}($link)[/$exerciseTag]",
                            id = "Exercise:$exerciseId",
                            icon = AutoCompleteIcon.DrawableFromId(getExerciseTypeIconId(exercise))
                        )
                    }

                val lectureAutoCompleteItems = course.lectures
                    .filter { query in it.title }
                    .mapNotNull { lecture ->
                        val lectureId = lecture.id ?: return@mapNotNull null
                        val link = LectureDeeplinks.ToLecture.markdownLink(courseId, lectureId)

                        AutoCompleteHint(
                            hint = lecture.title,
                            replacementText = "[lecture]${lecture.title}($link)[/lecture]",
                            id = "Lecture:$lectureId",
                            icon = AutoCompleteIcon.DrawableFromImageVector(Icons.Default.School)
                        )
                    }

                listOf(
                    AutoCompleteHintCollection(
                        type = AutoCompleteType.EXERCISES,
                        items = exerciseAutoCompleteItems
                    ),
                    AutoCompleteHintCollection(
                        type = AutoCompleteType.LECTURES,
                        items = lectureAutoCompleteItems
                    )
                )
            }
        }

    private fun produceConversationAutoCompleteHints(query: String): Flow<DataState<List<AutoCompleteHintCollection>>> =
        conversations.map { conversationsDataState ->
            conversationsDataState.bind { conversations ->
                val conversationAutoCompleteItems = conversations
                    .filterIsInstance<ChannelChat>()
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map { channel ->
                        AutoCompleteHint(
                            hint = channel.name,
                            replacementText = "[channel]${channel.name}(${channel.id})[/channel]",
                            id = "Channel:${channel.id}",
                            icon = AutoCompleteIcon.DrawableFromImageVector(
                                getChannelIconImageVector(channel)
                            )
                        )
                    }

                listOf(
                    AutoCompleteHintCollection(
                        type = AutoCompleteType.CHANNELS,
                        items = conversationAutoCompleteItems
                    )
                )
            }
        }

    private fun produceFaqAutoCompletionHints(query: String): Flow<DataState<List<AutoCompleteHintCollection>>> {
        if (!isFaqEnabled) return flowOf(DataState.Success(emptyList()))

        return faqRepository.getFaqs().map { faqsDataState ->
            faqsDataState.bind { faqs ->
                val faqAutoCompleteItems = faqs
                    .filter { it.questionTitle.contains(query, ignoreCase = true) }
                    .map { faq ->
                        val link = FaqDeeplinks.ToFaq.markdownLink(courseId, faq.id)
                        AutoCompleteHint(
                            hint = faq.questionTitle,
                            replacementText = "[faq]${faq.questionTitle}($link)[/faq]",
                            id = "Faq:${faq.id}",
                            icon = AutoCompleteIcon.DrawableFromImageVector(Icons.Default.QuestionMark)
                        )
                    }

                listOf(
                    AutoCompleteHintCollection(
                        type = AutoCompleteType.FAQS,
                        items = faqAutoCompleteItems
                    )
                )
            }
        }
    }
}