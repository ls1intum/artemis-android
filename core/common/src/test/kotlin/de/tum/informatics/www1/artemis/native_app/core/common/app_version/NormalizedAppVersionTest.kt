package de.tum.informatics.www1.artemis.native_app.core.common.app_version

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class NormalizedAppVersionTest {

    @Test
    fun `test valid version string`() {
        val version = NormalizedAppVersion("1.2.3")
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
    }

    @Test
    fun `test valid two digit version string`() {
        val version = NormalizedAppVersion("1.2.30")
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(30, version.patch)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test invalid version string`() {
        NormalizedAppVersion("1.2")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test invalid version string with letters`() {
        NormalizedAppVersion("1.a.3")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test invalid version string with extra parts`() {
        NormalizedAppVersion("1.2.3.4")
    }

    @Test
    fun `test fromFullVersionName`() {
        val version = NormalizedAppVersion.fromFullVersionName("1.2.3-beta")
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
    }

    @Test
    fun `test compareTo`() {
        val version1 = NormalizedAppVersion("1.2.3")
        val version2 = NormalizedAppVersion("1.2.4")
        val version3 = NormalizedAppVersion("1.3.0")
        val version4 = NormalizedAppVersion("2.0.0")

        assertTrue(version1 < version2)
        assertTrue(version2 < version3)
        assertTrue(version3 < version4)
        assertTrue(version4 > version1)
    }

}