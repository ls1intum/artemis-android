package de.tum.informatics.www1.artemis.native_app.feature.force_update

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateRepository
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class UpdateRepositoryTest {

    private val updateRepository = UpdateRepository(
        context = mockk(relaxed = true),
        updateService = mockk(relaxed = true),
        version = "1.2.3",
        serverConfigurationService = mockk(relaxed = true),
        accountService = mockk(relaxed = true)
    )

    @Test
    fun `isVersionGreater should return true when server version is newer`() {
        assertTrue(updateRepository.isVersionGreater("1.2.4", "1.2.3"))
        assertTrue(updateRepository.isVersionGreater("2.0.0", "1.9.9"))
        assertTrue(updateRepository.isVersionGreater("1.3.0", "1.2.9"))
    }

    @Test
    fun `isVersionGreater should return false when server version is older or the same`() {
        assertFalse(updateRepository.isVersionGreater("1.2.3", "1.2.3"))
        assertFalse(updateRepository.isVersionGreater("1.2.2", "1.2.3"))
        assertFalse(updateRepository.isVersionGreater("0.9.9", "1.0.0"))
    }

}
