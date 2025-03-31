package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.ServerTimeServiceStub
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.websocket.test.LiveParticipationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl.ChannelServiceStub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@PlayStoreScreenshots
@Composable
fun `Lecture - Overview`() {
    val viewModel = LectureViewModel(
        lectureId = 0L,
        networkStatusProvider = NetworkStatusProviderStub(),
        lectureService = object : LectureService {
            override suspend fun loadLecture(
                lectureId: Long,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<Lecture> = NetworkResponse.Response(
                Lecture(
                    id = 1L,
                    title = "Lecture 7 - Rocket Fuel ⛽",
                    description = "In this lecture, you will learn about the most important types of rocket fuel.",
                    lectureUnits = listOf(
                        LectureUnitText(
                            id = 2L,
                            name = "Introduction to Fuel Types",
                            content = """
                            |Multiple types of rocket propellants exist. Each of them has their own advantages
                            |and disadvantages.
                            |1. Solid chemical propellants
                            |2. Liquid chemical propellants
                            |3. Hybrid propellants
                            """.trimMargin()
                        ),
                        LectureUnitText(
                            id = 3L,
                            name = "Solid Chemical Propellants",
                            content = """
                                |Solid chemical propellants are a fundamental component of 
                                |rocket engines, offering simplicity and reliability in space
                                |exploration. Comprising a mixture of fuel and oxidizer tightly
                                |packed into a solid form, these propellants provide thrust by
                                |controlled combustion. Their sturdiness and efficiency make 
                                |them a vital choice for various rocket applications.
                            """.trimMargin()
                        )
                    )
                )
            )

            override suspend fun completeLectureUnit(
                lectureUnitId: Long,
                lectureId: Long,
                completed: Boolean,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<Unit> = NetworkResponse.Response(Unit)
        },
        serverConfigurationService = ServerConfigurationServiceStub(),
        accountService = AccountServiceStub(),
        liveParticipationService = LiveParticipationServiceStub(),
        savedStateHandle = SavedStateHandle(),
        channelService = ChannelServiceStub,
        serverTimeService = ServerTimeServiceStub(),
        courseExerciseService = object : CourseExerciseService {
            override val onReloadRequired: Flow<Unit> = emptyFlow()
            override suspend fun startExercise(exerciseId: Long): NetworkResponse<Participation> =
                NetworkResponse.Response(StudentParticipation.StudentParticipationImpl())
        }
    )

    ScreenshotFrame(title = "Directly interact with your lectures within the app") {
        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = 0L,
            lectureId = 0L,
            viewModel = viewModel,
            navController = NavController(LocalContext.current),
            onViewExercise = {},
            onNavigateToExerciseResultView = {},
            onNavigateToTextExerciseParticipation = { _, _ -> },
            onParticipateInQuiz = { _, _ -> },
            onClickViewQuizResults = { _, _ -> }
        )
    }
}