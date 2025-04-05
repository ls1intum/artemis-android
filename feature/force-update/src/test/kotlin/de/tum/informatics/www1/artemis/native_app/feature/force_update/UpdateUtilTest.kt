package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateUtil
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateServiceResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class UpdateUtilTest {

    private val version1_1_9 = NormalizedAppVersion("1.1.9")
    private val version1_2_3 = NormalizedAppVersion("1.2.3")
    private val version1_3_0 = NormalizedAppVersion("1.3.0")

    @Test
    fun `processUpdateResponse should detect update if server min version is higher`() {
        val response = NetworkResponse.Response(
            UpdateServiceResult(
                minVersion = version1_3_0,
                recommendedVersion = version1_3_0,
                features = listOf("new-feature")
            )
        )

        val result = UpdateUtil.processUpdateResponse(response, currentVersion = version1_2_3)

        assertTrue(result.updateAvailable)
        assertTrue(result.forceUpdate)
        assertEquals(version1_2_3, result.currentVersion)
        assertEquals(version1_3_0, result.minVersion)
    }

    @Test
    fun `processUpdateResponse should not detect update if server min version is equal`() {
        val response = NetworkResponse.Response(
            UpdateServiceResult(
                minVersion = version1_2_3,
                recommendedVersion = version1_3_0,
                features = emptyList()
            )
        )

        val result = UpdateUtil.processUpdateResponse(response, currentVersion = version1_2_3)

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_2_3, result.minVersion)
    }

    @Test
    fun `processUpdateResponse should not detect update if server version is older`() {
        val response = NetworkResponse.Response(
            UpdateServiceResult(
                minVersion = version1_1_9,
                recommendedVersion = version1_2_3,
                features = emptyList()
            )
        )

        val result = UpdateUtil.processUpdateResponse(response, currentVersion = version1_2_3)

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_1_9, result.minVersion)
    }

    @Test
    fun `processUpdateResponse should handle network failure`()  = runBlocking {
        val result = UpdateUtil.processUpdateResponse(
            response = NetworkResponse.Failure(Exception()),
            currentVersion = version1_2_3,
        )

        assertFalse(result.updateAvailable)
        assertFalse(result.forceUpdate)
        assertEquals(version1_2_3, result.minVersion)
    }

    @Test
    fun `processUpdateResponse should enable feature flag when present`() {
        FeatureAvailability.setAvailableFeatures(emptyList()) //reset

        val response = NetworkResponse.Response(
            UpdateServiceResult(
                minVersion = version1_3_0,
                recommendedVersion = version1_3_0,
                features = listOf("CourseSpecificNotifications")
            )
        )

        UpdateUtil.processUpdateResponse(response, version1_2_3)

        assertTrue(FeatureAvailability.isEnabled(Feature.CourseNotifications))
    }

    @Test
    fun `processUpdateResponse should not enable feature flag when absent`() {
        FeatureAvailability.setAvailableFeatures(emptyList()) //reset

        val response = NetworkResponse.Response(
            UpdateServiceResult(
                minVersion = version1_2_3,
                recommendedVersion = version1_3_0,
                features = emptyList()
            )
        )

        UpdateUtil.processUpdateResponse(response, version1_2_3)

        assertFalse(FeatureAvailability.isEnabled(Feature.CourseNotifications))
    }
}
