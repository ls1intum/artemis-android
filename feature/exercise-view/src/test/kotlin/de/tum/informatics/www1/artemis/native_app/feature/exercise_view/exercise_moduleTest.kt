package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.exerciseModule
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationViewModel
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
internal class exercise_moduleTest : BaseComposeTest() {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkKoinModule() {
        exerciseModule.verify(
            injections = listOf(
                definition<ExerciseViewModel>(Long::class),
                definition<TextExerciseParticipationViewModel>(Long::class, Long::class)
            ),
            extraTypes = listOf(
                ServerConfigurationService::class,
                NetworkStatusProvider::class,
                AccountService::class,
                CoroutineContext::class,
                ExerciseService::class,
                LiveParticipationService::class,
                CourseExerciseService::class
            )
        )
    }
}