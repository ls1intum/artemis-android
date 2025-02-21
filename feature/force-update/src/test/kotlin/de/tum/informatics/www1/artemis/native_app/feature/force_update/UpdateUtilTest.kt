package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class UpdateUtilTest {

    @Test
    fun `normalizeVersion should remove suffix after dash`() {
        assertEquals("1.2.3", UpdateUtil.normalizeVersion("1.2.3-prod"))
        assertEquals("2.0.0", UpdateUtil.normalizeVersion("2.0.0-beta"))
        assertEquals("3.5.1", UpdateUtil.normalizeVersion("3.5.1-alpha"))
        assertEquals("1.2.3", UpdateUtil.normalizeVersion("1.2.3"))
    }

    @Test
    fun `isVersionGreater should return true when server version is newer`() {
        assertTrue(UpdateUtil.isVersionGreater("1.2.4", "1.2.3"))
        assertTrue(UpdateUtil.isVersionGreater("2.0.0", "1.9.9"))
        assertTrue(UpdateUtil.isVersionGreater("1.3.0", "1.2.9"))
    }

    @Test
    fun `isVersionGreater should return false when server version is older or the same`() {
        assertFalse(UpdateUtil.isVersionGreater("1.2.3", "1.2.3"))
        assertFalse(UpdateUtil.isVersionGreater("1.2.2", "1.2.3"))
        assertFalse(UpdateUtil.isVersionGreater("0.9.9", "1.0.0"))
    }

    @Test
    fun `isTimeToCheckUpdate should return true if more than 2 days have passed`() {
        val lastCheck = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3) // 3 days ago
        val now = System.currentTimeMillis()

        assertTrue(UpdateUtil.isTimeToCheckUpdate(lastCheck, now))
    }

    @Test
    fun `isTimeToCheckUpdate should return false if less than 2 days have passed`() {
        val lastCheck = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1) // 1 day ago
        val now = System.currentTimeMillis()

        assertFalse(UpdateUtil.isTimeToCheckUpdate(lastCheck, now))
    }
}
