package de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post

import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisViewModel
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class CreateStandalonePostViewModel(
    private val metisContext: MetisContext,
    private val savedStateHandle: SavedStateHandle,
    metisService: MetisService,
    metisStorageService: MetisStorageService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider
) : MetisViewModel(metisService, metisStorageService, serverConfigurationService, accountService, serverDataService, networkStatusProvider) {

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
    fun createPost(onResponse: (MetisModificationResponse<String>) -> Unit) {
        val actualTags = savedStateHandle.get<String>(TAG_TAGS)
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val title = savedStateHandle.get<String>(TAG_TITLE)
        val content = savedStateHandle.get<String>(TAG_CONTENT)
        val courseWideContext = savedStateHandle.get<CourseWideContext>(TAG_COURSE_WIDE_CONTEXT)

        val post = StandalonePost(
            title = title,
            tags = actualTags,
            content = content,
            course = when (metisContext) {
                is MetisContext.Conversation -> null
                else -> Course(id = metisContext.courseId)
            },
            courseWideContext = courseWideContext,
            creationDate = Clock.System.now()
        )

        createStandalonePost(post, onResponse)
    }

    override suspend fun getMetisContext(): MetisContext = metisContext
}