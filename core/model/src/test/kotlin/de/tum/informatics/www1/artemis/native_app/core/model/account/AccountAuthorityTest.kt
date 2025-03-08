package de.tum.informatics.www1.artemis.native_app.core.model.account

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class AccountAuthorityTest {

    @Test
    fun `test comparable`() {
        assertTrue(AccountAuthority.ROLE_USER < AccountAuthority.ROLE_TA)
        assertTrue(AccountAuthority.ROLE_USER < AccountAuthority.ROLE_INSTRUCTOR)
        assertFalse(AccountAuthority.ROLE_ADMIN < AccountAuthority.ROLE_USER)
    }

    @Test
    fun `test max`() {
        assertEquals(AccountAuthority.ROLE_ADMIN, AccountAuthority.entries.toTypedArray().max())
        assertEquals(AccountAuthority.ROLE_TA, listOf(AccountAuthority.ROLE_USER, AccountAuthority.ROLE_TA).max())
    }
}