package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.create_standalone_post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateStandalonePostViewModel(
    val metisContext: MetisContext,
    private val savedStateHandle: SavedStateHandle,
    metisModificationService: MetisModificationService,
    metisStorageService: MetisStorageService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val exerciseService: ExerciseService
) : MetisViewModel(
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider
) {

    companion object {
        private const val TAG_COURSE_WIDE_CONTEXT = "TAG_COURSE_WIDE_CONTEXT"
        private const val TAG_TITLE = "TITLE"
        private const val TAG_TAGS = "TAGS"
        private const val TAG_CONTENT = "CONTENT"
    }

    val context: Flow<CourseWideContext> =
        savedStateHandle.getStateFlow(TAG_COURSE_WIDE_CONTEXT, CourseWideContext.TECH_SUPPORT)
    val title: Flow<String> = savedStateHandle.getStateFlow(TAG_TITLE, "")
    val tags: Flow<String> = savedStateHandle.getStateFlow(TAG_TAGS, "")
    val content: Flow<String> = savedStateHandle.getStateFlow(TAG_CONTENT, "")

    private val areTagsValid: Flow<Boolean> = tags.map { setTags ->
        val tagList = setTags.split(',')
        setTags.isBlank() || (tagList.size < 4 && tagList.none { it.isBlank() })
    }

    val canCreatePost: Flow<Boolean> =
        combine(title, content, areTagsValid) { title, content, areTagsValid ->
            title.isNotBlank() && content.isNotBlank() && areTagsValid
        }

    fun updateContext(newContext: CourseWideContext) {
        savedStateHandle[TAG_COURSE_WIDE_CONTEXT] = newContext
    }

    fun updateTitle(newTitle: String) {
        savedStateHandle[TAG_TITLE] = newTitle
    }

    fun updateTags(newTags: String) {
        savedStateHandle[TAG_TAGS] = newTags
    }

    fun updateContent(newContent: String) {
        savedStateHandle[TAG_CONTENT] = newContent
    }

    /**
     * @param onResponse contains the client side post id
     */
    fun createPost(onResponse: (MetisModificationResponse<String>) -> Unit): Job {
        val actualTags = savedStateHandle.get<String>(TAG_TAGS)
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val title = savedStateHandle.get<String>(TAG_TITLE)
        val content = savedStateHandle.get<String>(TAG_CONTENT)
        val courseWideContext = savedStateHandle.get<CourseWideContext>(TAG_COURSE_WIDE_CONTEXT)

        val course = when (metisContext) {
            is MetisContext.Conversation -> null
            else -> Course(id = metisContext.courseId)
        }

        return viewModelScope.launch {
            val post = StandalonePost(
                id = null,
                title = title,
                tags = actualTags,
                content = content,
                lecture = when (metisContext) {
                    is MetisContext.Lecture -> Lecture(id = metisContext.lectureId, course = course)
                    else -> null
                },
                course = when (metisContext) {
                    is MetisContext.Course -> course
                    else -> null
                },
                exercise = when (metisContext) {
                    is MetisContext.Exercise -> {
                        when (val exerciseResult = exerciseService.getExerciseDetails(
                            metisContext.exerciseId,
                            serverUrl = serverConfigurationService.serverUrl.first(),
                            authToken = accountService.authToken.first()
                        )) {
                            is NetworkResponse.Response -> convertExercise(exerciseResult)
                            is NetworkResponse.Failure -> {
                                onResponse(
                                    MetisModificationResponse.Failure(
                                        MetisModificationFailure.CREATE_POST
                                    )
                                )
                                return@launch
                            }
                        }
                    }
                    else -> null
                },
                courseWideContext = when (metisContext) {
                    is MetisContext.Course -> courseWideContext
                    else -> null
                },
                creationDate = Clock.System.now(),
                displayPriority = DisplayPriority.NONE
            )

            onResponse(createStandalonePostImpl(post))
        }
    }

    private fun convertExercise(exerciseResult: NetworkResponse.Response<Exercise>) =
        when (val base = exerciseResult.data) {
            is FileUploadExercise -> FileUploadExercise(id = base.id, title = base.title)
            is ModelingExercise -> ModelingExercise(id = base.id, title = base.title)
            is ProgrammingExercise -> ProgrammingExercise(id = base.id, title = base.title)
            is QuizExercise -> QuizExercise(id = base.id, title = base.title)
            is TextExercise -> TextExercise(id = base.id, title = base.title)
            is UnknownExercise -> UnknownExercise(id = base.id, title = base.title)
        }

    override suspend fun getMetisContext(): MetisContext = metisContext
}