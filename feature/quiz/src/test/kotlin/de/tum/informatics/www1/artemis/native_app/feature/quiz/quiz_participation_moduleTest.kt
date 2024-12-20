package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizResultViewModel
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
internal class quiz_participation_moduleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        quizParticipationModule.verify(
            injections = listOf(
                definition<QuizParticipationViewModel>(Long::class, Long::class, QuizType.WorkableQuizType::class),
                definition<QuizResultViewModel>(Long::class, QuizType.ViewableQuizType::class)
            ),
            extraTypes = listOf(
                SavedStateHandle::class,
                ServerConfigurationService::class,
                NetworkStatusProvider::class,
                AccountService::class,
                CoroutineContext::class,
                ExerciseService::class,
                QuizExerciseService::class,
                QuizParticipationService::class,
                WebsocketProvider::class,
                ParticipationService::class,
                ServerTimeService::class
            )
        )
    }
}