package de.tum.informatics.www1.artemis.native_app.core.ui.media

import org.junit.Test
import org.junit.Assert.*

@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureMediaPolicyTest {
    @Test fun directSourcesUseParsedCaseInsensitivePath() {
        assertEquals(MediaSourceKind.MP4, classifyMediaSource("https://a.test/movie.MP4?token=x"))
        assertEquals(MediaSourceKind.HLS, classifyMediaSource("https://a.test/movie.m3u8?token=x"))
        assertEquals(MediaSourceKind.WEB, classifyMediaSource("http://localhost:9000/watch?id=1"))
        assertEquals(MediaSourceKind.HLS, classifyMediaSource("https://a.test/stream", "application/vnd.apple.mpegurl"))
    }
    @Test fun invalidSourcesCannotReachPlayerOrBrowser() {
        listOf("javascript:alert(1)", "file:///tmp/a.mp4", "data:video/mp4,x", "https://", "", "https://user:secret@a.test/a.mp4").forEach {
            assertEquals(it, MediaSourceKind.INVALID, classifyMediaSource(it))
        }
    }
    @Test fun credentialsRequireExactNormalizedOrigin() {
        assertTrue(sameMediaOrigin("https://A.test", "https://a.test:443/file"))
        assertFalse(sameMediaOrigin("https://a.test", "https://a.test:444/file"))
        assertFalse(sameMediaOrigin("https://a.test", "http://a.test/file"))
        assertFalse(sameMediaOrigin("https://a.test", "https://a.test.evil/file"))
    }
    @Test fun progressIsScopedAndNeverUsesSignedQuery() {
        val a = mediaProgressKey("alice", "https://A.test:443/", "lecture/1/unit/2", "https://a.test/a.mp4?token=secret")
        assertEquals(a, mediaProgressKey("alice", "https://a.test", "lecture/1/unit/2", "https://a.test/a.mp4?token=changed"))
        assertNotEquals(a, mediaProgressKey("bob", "https://a.test", "lecture/1/unit/2", "https://a.test/a.mp4"))
        assertNotEquals(a, mediaProgressKey("alice", "https://b.test", "lecture/1/unit/2", "https://a.test/a.mp4"))
        assertNotEquals(a, mediaProgressKey("alice", "https://a.test", "lecture/1/unit/3", "https://a.test/a.mp4"))
        assertFalse(a.contains("secret"))
    }
    @Test fun explicitPageWinsAndClampsAfterDocumentLoad() {
        assertEquals(2, initialPdfPageIndex(3, 7, 10))
        assertEquals(6, initialPdfPageIndex(null, 7, 10))
        assertEquals(0, initialPdfPageIndex(0, 7, 10))
        assertEquals(0, initialPdfPageIndex(-5, 7, 10))
        assertEquals(9, initialPdfPageIndex(99, 7, 10))
        assertEquals(0, initialPdfPageIndex(null, null, 0))
    }
}
