package de.tum.informatics.www1.artemis.native_app.core.model.account

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the membership check, which reads the per-course roles the account endpoint returns. Before
 * Artemis 10 it compared the account's groups against course group names, and the server no longer
 * sends either of those.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class AccountCourseRoleTest {

    private val course = Course(id = 42L)

    private fun account(
        roles: Set<CourseRole>,
        courseId: Long = 42L,
        authorities: List<AccountAuthority> = emptyList()
    ) = Account(
        authorities = authorities,
        courseRoles = listOf(CourseAccessRights(courseId = courseId, roles = roles))
    )

    @Test
    fun `tutor role grants tutor access`() {
        assertTrue(account(setOf(CourseRole.TEACHING_ASSISTANT)).isAtLeastTutorInCourse(course))
    }

    @Test
    fun `editor and instructor outrank tutor`() {
        assertTrue(account(setOf(CourseRole.EDITOR)).isAtLeastTutorInCourse(course))
        assertTrue(account(setOf(CourseRole.INSTRUCTOR)).isAtLeastTutorInCourse(course))
    }

    @Test
    fun `student role does not grant tutor access`() {
        assertFalse(account(setOf(CourseRole.STUDENT)).isAtLeastTutorInCourse(course))
    }

    @Test
    fun `a role in another course does not leak into this one`() {
        assertFalse(
            account(setOf(CourseRole.INSTRUCTOR), courseId = 43L).isAtLeastTutorInCourse(course)
        )
    }

    @Test
    fun `an admin is a tutor everywhere`() {
        val admin = Account(
            authorities = listOf(AccountAuthority.ROLE_ADMIN),
            courseRoles = emptyList()
        )
        assertTrue(admin.isAtLeastTutorInCourse(course))
    }

    @Test
    fun `a super admin is a tutor everywhere`() {
        val superAdmin = Account(
            authorities = listOf(AccountAuthority.ROLE_SUPER_ADMIN),
            courseRoles = emptyList()
        )
        assertTrue(superAdmin.isAtLeastTutorInCourse(course))
    }

    @Test
    fun `the super admin authority decodes rather than failing the whole account`() {
        // The internal administrator holds ROLE_SUPER_ADMIN. An unknown name inside a list of enums
        // throws instead of being coerced, so a missing constant would break the account entirely.
        val json = """
            {"id":1,"login":"artemis_admin","activated":true,
             "authorities":["ROLE_USER","ROLE_SUPER_ADMIN"]}
        """.trimIndent()

        val account = Json { ignoreUnknownKeys = true }.decodeFromString<Account>(json)

        assertTrue(account.authorities.contains(AccountAuthority.ROLE_SUPER_ADMIN))
        assertTrue(account.isAtLeastTutorInCourse(course))
    }

    @Test
    fun `the course id alone answers the check`() {
        assertTrue(account(setOf(CourseRole.EDITOR)).isAtLeastTutorInCourse(42L))
        assertFalse(account(setOf(CourseRole.EDITOR)).isAtLeastTutorInCourse(43L))
        assertFalse(account(setOf(CourseRole.EDITOR)).isAtLeastTutorInCourse(null))
    }

    @Test
    fun `a global instructor authority alone grants nothing in a course`() {
        val instructor = Account(
            authorities = listOf(AccountAuthority.ROLE_INSTRUCTOR),
            courseRoles = emptyList()
        )
        assertFalse(instructor.isAtLeastTutorInCourse(course))
    }

    @Test
    fun `no roles at all is not a tutor`() {
        assertFalse(Account().isAtLeastTutorInCourse(course))
    }

    @Test
    fun `course roles decode from the shape the account endpoint sends`() {
        // Trimmed from a real GET api/core/public/account response.
        val json = """
            {
              "id": 7,
              "login": "ab12cde",
              "activated": true,
              "courseRoles": [
                { "courseId": 42, "roles": ["STUDENT", "TEACHING_ASSISTANT"] },
                { "courseId": 43, "roles": ["INSTRUCTOR"] }
              ]
            }
        """.trimIndent()

        val account = Json { ignoreUnknownKeys = true }.decodeFromString<Account>(json)

        assertEquals(2, account.courseRoles.size)
        assertTrue(account.isAtLeastTutorInCourse(course))
        assertTrue(account.isAtLeastTutorInCourse(Course(id = 43L)))
        assertFalse(account.isAtLeastTutorInCourse(Course(id = 44L)))
    }
}
