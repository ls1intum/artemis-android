package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisServiceStub
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MetisSearchPagingSourceTest {

    private val metisServiceStub = MetisServiceStub()
    private val sut: MetisSearchPagingSource = MetisSearchPagingSource(
        metisService = metisServiceStub,
        context = MetisService.StandalonePostsContext(
            metisContext = MetisContext.Course(1),
            filter = emptyList(),
            query = null
        ),
        authToken = "token",
        serverUrl = "url"
    )

    @Test
    fun `test GIVEN the metisService returns duplicated posts WHEN calling the load method THEN only unique posts are returned`() = runTest {
        // GIVEN
        val post1 = StandalonePost(id = 1)
        val post2 = StandalonePost(id = 2)

        val posts = listOf(post1, post2, post1)
        metisServiceStub.posts = posts

        // WHEN
        val result = sut.load(PagingSource.LoadParams.Refresh(0, 10, false))

        // THEN
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page<Int, StandalonePost>
        assertEquals(2, page.data.size)
        assertEquals(post1, page.data[0])
        assertEquals(post2, page.data[1])
    }

}