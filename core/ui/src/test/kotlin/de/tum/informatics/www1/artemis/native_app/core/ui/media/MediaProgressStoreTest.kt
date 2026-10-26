package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class MediaProgressStoreTest {
    @Test fun navigationAfterExplicitTargetUpdatesSavedPositionForReopen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = MediaProgressStore(context)
        val key = mediaProgressKey("alice", "https://a.test", "lecture/1/attachment/2", "https://a.test/a.pdf?token=x")
        store.save(key, 7)
        assertEquals(2, initialPdfPageIndex(3, store.read(key)?.toInt(), 10))
        // The user turns to page 5 after the initial jump; later observation must replace page 3.
        store.save(key, 5)
        val reopened = MediaProgressStore(context)
        assertEquals(4, initialPdfPageIndex(null, reopened.read(key)?.toInt(), 10))
        assertNull(reopened.read(mediaProgressKey("bob", "https://a.test", "lecture/1/attachment/2", "https://a.test/a.pdf")))
        assertNull(reopened.read(mediaProgressKey("alice", "https://b.test", "lecture/1/attachment/2", "https://a.test/a.pdf")))
    }
}
