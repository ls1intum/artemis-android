package de.tum.informatics.www1.artemis.native_app.feature.coremodulestest

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.android.appModule
import de.tum.informatics.www1.artemis.native_app.core.common.CurrentActivityListener
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.UserIdentifier
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.CoroutineContext

@OptIn(KoinExperimentalAPI::class)
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class AppModuleTest {

    @Test
    fun checkKoinModule() {

        // Verify Koin configuration
        appModule.verify(
            extraTypes = listOf(
                CoroutineContext::class,
                SavedStateHandle::class,
                Boolean::class,
                Long::class,
                UserIdentifier::class,
                StandalonePostId::class,
                Application::class,
                CurrentActivityListener::class,
                Context::class,
                WorkerParameters::class,
                QuizType.WorkableQuizType::class,
                QuizType.ViewableQuizType::class,
                SavedPostStatus::class,
            )
        )
    }
}