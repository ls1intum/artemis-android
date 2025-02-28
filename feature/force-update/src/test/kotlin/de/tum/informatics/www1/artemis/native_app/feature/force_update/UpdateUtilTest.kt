package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
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

    private val version1_1_9 = NormalizedAppVersion("1.1.9")
    private val version1_2_3 = NormalizedAppVersion("1.2.3")
    private val version1_3_0 = NormalizedAppVersion("1.3.0")

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
        var detectedVersion: NormalizedAppVersion? = null

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = NetworkResponse.Response(version1_3_0),
            currentVersion = version1_2_3,
            storedServerVersion = version1_2_3,
            onUpdateDetected = { newVersion ->
                detectedVersion = newVersion
            }
        )

        assertTrue(result.updateAvailable)
        assertTrue(result.forceUpdate)
        assertEquals(version1_2_3, result.currentVersion)
        assertEquals(version1_3_0, result.minVersion)
        assertEquals(version1_3_0, detectedVersion)
    }



    @Test
    fun `createUpdateResultBasedOnServiceResponse should not detect update if server version is same`() = runBlocking {
        var detectedVersion: NormalizedAppVersion? = null

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = NetworkResponse.Response(version1_2_3),
            currentVersion = version1_2_3,
            storedServerVersion = version1_2_3,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_2_3, result.currentVersion)
        assertEquals(version1_2_3, result.minVersion)
        assertEquals(null, detectedVersion)
    }

    @Test
    fun `createUpdateResultBasedOnServiceResponse should not detect update if server version is older`() = runBlocking {
        var detectedVersion: NormalizedAppVersion? = null

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = NetworkResponse.Response(version1_1_9),
            currentVersion = version1_2_3,
            storedServerVersion = version1_2_3,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_2_3, result.currentVersion)
        assertEquals(version1_1_9, result.minVersion)
        assertEquals(null, detectedVersion)
    }

    @Test
    fun `createUpdateResultBasedOnServiceResponse should return no update needed if network failure`() = runBlocking {
        var detectedVersion: NormalizedAppVersion? = null

        val result = UpdateUtil.createUpdateResultBasedOnServiceResponse(
            response = NetworkResponse.Failure(Exception()),
            currentVersion = version1_2_3,
            storedServerVersion = NormalizedAppVersion.ZERO,
            onUpdateDetected = { detectedVersion = it }
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_2_3, result.currentVersion)
        assertEquals(NormalizedAppVersion.ZERO, result.minVersion)
        assertEquals(null, detectedVersion)
    }
}
