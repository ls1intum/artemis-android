package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class PostActionBarUITest: BaseChatUITest() {

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view THEN Edit action is shown`() {

    }

    @Test
    fun `test GIVEN a user with moderation-rights WHEN navigating to the thread view THEN Edit action is not shown`() {

    }

    @Test
    fun `test GIVEN a user with moderation-rights WHEN navigating to the thread view THEN delete option is shown`() {

    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view as the post author THEN delete option is shown`() {

    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view as non-moderator THEN delete option is not shown`() {

    }
}