package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.impl.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.test.TestArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ArtemisContextBasedServiceImplTest {

    private val initialContext = ArtemisContextImpl.LoggedIn("server", "token", "user")

    private val ktorProvider = mockk<KtorProvider>()
    private val artemisContextFlow = MutableStateFlow<ArtemisContext>(initialContext)
    private val artemisContextProvider = TestArtemisContextProvider(stateFlow = artemisContextFlow)

    @Test
    fun `test GIVEN a LoggedInBasedService WHEN emitting a new LoggedIn context THEN onArtemisContextChanged emits`() {
        val sut = object : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider) {}

        val emittedContexts = mutableListOf<ArtemisContext>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            sut.onArtemisContextChanged.collect { context ->
                emittedContexts.add(context)
            }
        }

        // Initial state is already emitted when collection starts
        assertEquals(1, emittedContexts.size)

        // Emit a new context
        val newContext = ArtemisContextImpl.LoggedIn("new-server", "new-token", "new-user")
        artemisContextFlow.value = newContext

        // Verify new context was emitted
        assertEquals(2, emittedContexts.size)
        assertEquals(newContext, emittedContexts[1])

        job.cancel()
    }

    @Test
    fun `test GIVEN a LoggedInBasedService WHEN emitting the same context THEN onArtemisContextChanged does not emit again`() {
        val sut = object : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider) {}

        val emittedContexts = mutableListOf<ArtemisContext>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            sut.onArtemisContextChanged.collect { context ->
                emittedContexts.add(context)
            }
        }

        assertEquals(1, emittedContexts.size)
        artemisContextFlow.value = initialContext.copy()

        // Verify no new emission happened
        assertEquals(1, emittedContexts.size)

        job.cancel()
    }

    @Test
    fun `test GIVEN a LoggedInBasedService WHEN emitting a Course context with same login data THEN onArtemisContextChanged does not emit again`() {
        val sut = object : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider) {}

        val emittedContexts = mutableListOf<ArtemisContext>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            sut.onArtemisContextChanged.collect { context ->
                emittedContexts.add(context)
            }
        }

        assertEquals(1, emittedContexts.size)

        // Create a Course context with same login data
        val initialContext = artemisContextFlow.value as ArtemisContextImpl.LoggedIn
        val courseContext = ArtemisContextImpl.Course(
            serverUrl = initialContext.serverUrl,
            authToken = initialContext.authToken,
            loginName = initialContext.loginName,
            account = Account(),
            courseId = 123L
        )

        // Emit the course context
        artemisContextFlow.value = courseContext

        // Verify no new emission happened since LoggedInBasedService only cares about LoggedIn contexts
        assertEquals(1, emittedContexts.size)

        job.cancel()
    }

    @Test
    fun `test GIVEN a CourseBasedService WHEN context changes from Course to LoggedIn THEN onArtemisContextChanged does not emit again`() {
        // Create a Course context initially
        val initialLoggedInContext = ArtemisContextImpl.LoggedIn("server", "token", "user")
        val initialCourseContext = ArtemisContextImpl.Course(
            serverUrl = initialLoggedInContext.serverUrl,
            authToken = initialLoggedInContext.authToken,
            loginName = initialLoggedInContext.loginName,
            account = Account(),
            courseId = 123L
        )

        // Set initial course context
        artemisContextFlow.value = initialCourseContext

        // Create CourseBasedService
        val sut = object : CourseBasedServiceImpl(ktorProvider, artemisContextProvider) {}

        val emittedContexts = mutableListOf<ArtemisContext>()
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            sut.onArtemisContextChanged.collect { context ->
                emittedContexts.add(context)
            }
        }

        // Initial state is already emitted when collection starts
        assertEquals(1, emittedContexts.size)
        assertTrue(emittedContexts[0] is ArtemisContextImpl.Course)

        // Change from Course to LoggedIn state
        artemisContextFlow.value = initialLoggedInContext

        // Verify no new emission happened since CourseBasedService only cares about Course contexts
        assertEquals(1, emittedContexts.size)

        job.cancel()
    }
}