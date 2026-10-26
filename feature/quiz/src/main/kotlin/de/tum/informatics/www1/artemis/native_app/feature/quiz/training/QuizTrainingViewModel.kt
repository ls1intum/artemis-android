package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import kotlinx.coroutines.launch

internal class QuizTrainingViewModel(
    courseId: Long,
    provider: KtorProvider,
    contextProvider: ArtemisContextProvider,
) : ViewModel() {
    val captured = contextProvider.stateFlow.value as? ArtemisContext.LoggedIn
    val session =
        QuizTrainingSession(
            if (captured != null)
                QuizTrainingService(provider, captured.serverUrl, captured.authToken, courseId)
            else
                object : QuizTrainingApi {
                    override suspend fun loadOverview(): TrainingOverview = error("No access")

                    override suspend fun loadSettings(): TrainingPrivacy = error("No access")

                    override suspend fun updatePrivacy(show: Boolean) = Unit

                    override suspend fun loadPage(request: TrainingPageRequest): TrainingPage =
                        error("No access")

                    override suspend fun submit(
                        question: TrainingQuestion,
                        payload: kotlinx.serialization.json.JsonObject,
                    ): de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer =
                        error("No access")
                }
        )

    init {
        viewModelScope.launch {
            contextProvider.stateFlow.collect { current ->
                if (
                    captured == null ||
                        current !is ArtemisContext.LoggedIn ||
                        current.serverUrl != captured.serverUrl ||
                        current.authToken != captured.authToken ||
                        current.loginName != captured.loginName ||
                        (current is ArtemisContext.Course && current.courseId != courseId)
                )
                    session.invalidate()
            }
        }
        act { loadOverview() }
    }

    fun act(action: suspend QuizTrainingSession.() -> Unit) {
        viewModelScope.launch { session.action() }
    }
}
