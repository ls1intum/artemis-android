package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.FileTextExerciseDraftStore
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraft
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraftKey
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import java.io.File
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseEditingCoordinator
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

@Category(UnitTest::class)
class TextExerciseDraftStoreTest {
    private fun newStore(directory: File) = FileTextExerciseDraftStore(directory, coordinator = TextExerciseEditingCoordinator(CoroutineScope(SupervisorJob() + Dispatchers.Default)))
    @get:Rule val folder = TemporaryFolder()
    private val key = TextExerciseDraftKey("https://server.example", "student", 1, 2)

    @Test fun `queued writes survive editor recreation and recover the newest exact text`() = runBlocking {
        val directory = folder.newFolder()
        val store = newStore(directory)
        store.load(key, "one")
        store.save(key, "one", TextExerciseDraft("First"))
        store.save(key, "one", TextExerciseDraft("**Final**\n🧪\n")).await()
        assertEquals("**Final**\n🧪\n", newStore(directory).load(key, "two")?.text)
    }

    @Test fun `draft recovery is isolated by server account exercise and participation`() = runBlocking {
        val store = newStore(folder.newFolder())
        store.load(key, "one")
        store.save(key, "one", TextExerciseDraft("Private")).await()
        listOf(key.copy(server = "https://other.example"), key.copy(account = "other"), key.copy(exerciseId = 9), key.copy(participationId = 9))
            .forEach { assertNull(store.load(it, "one")) }
        assertEquals("Private", store.load(key, "one")?.text)
    }

    @Test fun `a superseded editor cannot replace a newer editor durable draft`() = runBlocking {
        val store = newStore(folder.newFolder())
        store.load(key, "older")
        store.save(key, "older", TextExerciseDraft("Older")).await()
        store.load(key, "newer")
        store.save(key, "newer", TextExerciseDraft("Newer")).await()
        assertEquals(false, store.save(key, "older", TextExerciseDraft("Late acknowledgement")).await())
        assertEquals("Newer", store.load(key, "newer")?.text)
    }

    @Test fun `failed atomic replacement is reported instead of claiming the draft was saved`() = runBlocking {
        val directory = folder.newFolder()
        val store = newStore(directory)
        store.load(key, "one")
        store.save(key, "one", TextExerciseDraft("Original")).await()
        val destination = directory.listFiles()!!.single()
        check(destination.delete())
        check(destination.mkdir())
        assertFails { store.save(key, "one", TextExerciseDraft("Cannot replace a directory")).await() }
        Unit
    }

    @Test fun `write failure is reported without killing the persistence queue`() = runBlocking {
        val blockedDirectory = folder.newFile()
        val store = newStore(blockedDirectory)
        store.coordinator.claim(key, "one")
        assertFails { store.save(key, "one", TextExerciseDraft("Keep me")).await() }
        check(blockedDirectory.delete())
        store.save(key, "one", TextExerciseDraft("Recovered")).await()
        assertEquals("Recovered", store.load(key, "one")?.text)
    }
}
