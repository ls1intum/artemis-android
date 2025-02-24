package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateUtil
import kotlinx.coroutines.runBlocking
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

    @Test
    fun `createUpdateResultBasedOnServiceResponse should detect update if server version is newer`() = runBlocking {
        var detectedVersion: String? = null
        val response: NetworkResponse.Response<String?> = NetworkResponse.Response("1.3.0")
        val currentVersion = "1.2.3"
        val storedServerVersion = "1.2.3"

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = response,
            currentVersion = currentVersion,
            storedServerVersion = storedServerVersion,
            onUpdateDetected = { newVersion ->
                detectedVersion = newVersion
            }
        )

        assertTrue(result.updateAvailable)
        assertTrue(result.forceUpdate)
        assertEquals(currentVersion, result.currentVersion)
        assertEquals("1.3.0", result.minVersion)
        assertEquals("1.3.0", detectedVersion)
    }

    @Test
    fun `createUpdateResultBasedOnServiceResponse should not detect update if server version is same`() = runBlocking {
        var detectedVersion: String? = null
        val response: NetworkResponse.Response<String?> = NetworkResponse.Response("1.2.3")
        val currentVersion = "1.2.3"
        val storedServerVersion = "1.2.3"

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = response,
            currentVersion = currentVersion,
            storedServerVersion = storedServerVersion,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(currentVersion, result.currentVersion)
        assertEquals("1.2.3", result.minVersion)
        assertEquals(null, detectedVersion)
    }

    @Test
    fun `createUpdateResultBasedOnServiceResponse should not detect update if server version is older`() = runBlocking {
        var detectedVersion: String? = null
        val response: NetworkResponse.Response<String?> = NetworkResponse.Response("1.1.9")
        val currentVersion = "1.2.3"
        val storedServerVersion = "1.2.3"

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = response,
            currentVersion = currentVersion,
            storedServerVersion = storedServerVersion,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(currentVersion, result.currentVersion)
        assertEquals("1.1.9", result.minVersion)
        assertEquals(null, detectedVersion)
    }

    @Test
    fun `createUpdateResultBasedOnServiceResponse should return no update needed if network failure`() = runBlocking {
        var detectedVersion: String? = null
        val response: NetworkResponse.Response<String?> = NetworkResponse.Response(null)
        val currentVersion = "1.2.3"
        val fallbackVersion = "0.0.0"

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = response,
            currentVersion = currentVersion,
            storedServerVersion = fallbackVersion,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(currentVersion, result.currentVersion)
        assertEquals(fallbackVersion, result.minVersion)
        assertEquals(null, detectedVersion)
    }
}
