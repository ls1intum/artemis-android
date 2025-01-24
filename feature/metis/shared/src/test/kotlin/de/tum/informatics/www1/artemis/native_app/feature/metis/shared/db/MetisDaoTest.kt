package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisDatabaseProviderMock
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisTestDatabase
import de.tum.informatics.www1.artemis.native_app.feature.metistest.loadAsList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class MetisDaoTest {

    private val serverId = "host"
    private val courseId = 1L
    private val conversationId = 1L
    private val clientPostId = "clientPostId"

    private val user = MetisUserEntity(
        serverId = serverId,
        id = 4,
        displayName = "User4",
        imageUrl = null,
    )
    private val basePost = BasePostingEntity(
        postId = clientPostId,
        serverId = serverId,
        postingType = BasePostingEntity.PostingType.STANDALONE,
        authorId = user.id,
        creationDate = Clock.System.now(),
        updatedDate = Clock.System.now(),
        content = "post content",
        authorRole = UserRole.USER,
        isSaved = false,
    )
    private val metisContext = MetisPostContextEntity(
        serverId = serverId,
        courseId = courseId,
        conversationId = conversationId,
        serverPostId = 1,
        clientPostId = clientPostId,
        postingType = BasePostingEntity.PostingType.STANDALONE,
    )
    private val post = StandalonePostingEntity(
        postId = clientPostId,
        title = null,
        context = null,
        displayPriority = BasePostingEntity.DisplayPriority.NONE,
        resolved = false,
        liveCreated = false
    )
    private val reaction = PostReactionEntity(
        postId = clientPostId,
        authorId = user.id,
        serverId = serverId,
        emojiId = "emojiId",
        id = 1,
    )

    private lateinit var database: MetisTestDatabase
    private lateinit var metisDao: MetisDao

    @Before
    fun setup() {
        val databaseProviderMock = MetisDatabaseProviderMock(InstrumentationRegistry.getInstrumentation().context)
        database = databaseProviderMock.database
        metisDao = database.metisDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAddPost() = runBlocking {
        // GIVEN: A inserted post
        insertStandalonePost()

        // WHEN: Querying the post
        val storedPosts = metisDao.queryCoursePosts(
            courseId = courseId,
            conversationId = conversationId,
            serverId = serverId,
        ).loadAsList()

        // THEN: Return post with correct userId and clientPostId
        assertEquals(1, storedPosts.size)
        val storedPost = storedPosts[0]
        assertEquals(user.id, storedPost.authorId)
        assertEquals(clientPostId, storedPost.clientPostId)
    }

    @Test
    fun testDeletePost() = runBlocking {
        // GIVEN: A inserted post
        insertStandalonePost()

        // WHEN: Deleting the post
        metisDao.deletePostingWithClientSideId(clientPostId)

        // THEN: The post is deleted in both tables
        database.query("SELECT * FROM standalone_postings", args = null).use {
            assertEquals(0, it.count)
        }
        database.query("SELECT * FROM postings", args = null).use {
            assertEquals(0, it.count)
        }

        // AND: The post context is deleted
        database.query("SELECT * FROM metis_post_context", args = null).use {
            assertEquals(0, it.count)
        }
    }

    @Test
    fun testAddPostWithReaction() = runBlocking {
        // GIVEN: A inserted post with a reaction
        insertStandalonePost()
        metisDao.insertReactions(listOf(reaction))

        // WHEN: Querying the post
        val storedPost = metisDao.queryCoursePosts(
            courseId = courseId,
            conversationId = conversationId,
            serverId = serverId,
        ).loadAsList()[0]

        // THEN: The reaction is stored
        assertEquals(1, storedPost.reactions.size)
        assertEquals(reaction.emojiId, storedPost.reactions[0].emojiId)
    }

    @Test
    fun testDeletePostWithReaction() = runBlocking {
        // GIVEN: A inserted post with a reaction
        insertStandalonePost()
        metisDao.insertReactions(listOf(reaction))

        // WHEN: Deleting the post
        metisDao.deletePostingWithClientSideId(clientPostId)

        // THEN: The reaction is deleted
        database.query("SELECT * FROM reactions", args = null).use {
            assertEquals(0, it.count)
        }
    }


    private suspend fun insertStandalonePost() {
        metisDao.insertUser(user)
        metisDao.insertBasePost(basePost)
        metisDao.insertPost(post)
        metisDao.insertPostMetisContext(metisContext)
    }
}