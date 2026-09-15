package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The server time endpoint is served by a servlet container valve that writes
 * [java.time.Instant.toString] as text/plain, so the client parses the body itself rather than
 * letting JSON content negotiation decode it. These are real responses from that endpoint.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ServerTimeFormatTest {

    @Test
    fun `parses the nanosecond precision the valve writes`() {
        val parsed = Instant.parse("2026-09-15T08:12:44.331649633Z")

        assertEquals(1_789_459_964L, parsed.epochSeconds)
        assertEquals(331649633, parsed.nanosecondsOfSecond)
    }

    @Test
    fun `parses a whole second without a fraction`() {
        val parsed = Instant.parse("2026-09-15T08:12:44Z")

        assertEquals(1_789_459_964L, parsed.epochSeconds)
        assertEquals(0, parsed.nanosecondsOfSecond)
    }

    @Test
    fun `trimming makes a trailing newline harmless`() {
        assertEquals(
            Instant.parse("2026-09-15T08:12:44.331649633Z"),
            Instant.parse("2026-09-15T08:12:44.331649633Z\n".trim())
        )
    }

    @Test
    fun `milliseconds survive the conversion the clock sync does`() {
        val parsed = Instant.parse("2026-09-15T08:12:44.331649633Z")

        assertEquals(1_789_459_964_331L, parsed.toEpochMilliseconds())
    }
}
