package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class EmojiCategoryListUtilTest {

    private val emoji = Emoji("emojiId", "unicode")


    @Test
    fun `test GIVEN a list of categories WHEN calling the util function THEN the correct header indices are returned`() {
        val emojiCategoryList = listOf(
            EmojiCategory(EmojiCategory.Id.RECENT, listOf(emoji, emoji, emoji)),
            EmojiCategory(EmojiCategory.Id.PEOPLE, listOf(emoji)),
            EmojiCategory(EmojiCategory.Id.OBJECTS, listOf())
        )

        val headerIndices = EmojiCategoryListUtil.getHeaderIndices(emojiCategoryList)

        assertEquals(listOf(0, 4, 6), headerIndices)
    }

    @Test
    fun `test GIVEN an empty recent category WHEN calling the util function THEN the empty message header is considered`() {
        val emojiCategoryList = listOf(
            EmojiCategory(EmojiCategory.Id.RECENT, emptyList()),
            EmojiCategory(EmojiCategory.Id.PEOPLE, listOf(emoji))
        )

        val headerIndices = EmojiCategoryListUtil.getHeaderIndices(emojiCategoryList)

        assertEquals(listOf(0, 2), headerIndices)
    }
}