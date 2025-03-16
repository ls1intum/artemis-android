package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContains

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class EmojiSearchUtilTest {

    private val heartEmoji = Emoji("heart", "❤️")
    private val blueHeartEmoji = Emoji("blue_heart", "💙")
    private val heartEyesEmoji = Emoji("heart_eyes", "😍")
    private val smilingFaceWith3HeartsEmoji = Emoji("smiling_face_with_3_hearts", "🥰")
    private val heartCatEyes = Emoji("heart_cat_eyes", "😻")
    private val smileEmoji = Emoji("smile", "😊")
    private val earEmoji = Emoji("ear", "👂")



    // -------------------- FILTERING TESTS --------------------

    @Test
    fun `test GIVEN a list of emojis WHEN searching THEN emojis with keywords containing the query are returned`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(heartEmoji, blueHeartEmoji, smileEmoji),
            query = "heart"
        )

        assertContains(result, heartEmoji)
        assertContains(result, blueHeartEmoji)
        assertFalse(result.contains(smileEmoji))
    }

    @Test
    fun `test GIVEN an emoji WHEN searching with uppercase THEN the emoji is returned`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(heartEmoji),
            query = "HEART"
        )

        assertContains(result, heartEmoji)
    }

    @Test
    fun `test GIVEN two emojis WHEN searching a substring of a keyword THEN only the matching emoji is returned`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(heartEmoji, earEmoji),
            query = "ear"
        )

        assertContains(result, earEmoji)
        assertFalse(result.contains(heartEmoji))
    }




    // -------------------- ORDERING TESTS --------------------

    @Test
    fun `test GIVEN an unordered list of emojis WHEN searching and sorting THEN emojis are sorted so that emojis starting with the query are in front of matches that come later in the emojiId`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(smilingFaceWith3HeartsEmoji, heartEmoji, blueHeartEmoji),
            query = "heart"
        )

        assertEquals(0, result.indexOf(heartEmoji))
        assertEquals(1, result.indexOf(blueHeartEmoji))
        assertEquals(2, result.indexOf(smilingFaceWith3HeartsEmoji))
    }

    @Test
    fun `test GIVEN one exact match and one partially match WHEN sorting THEN the exact match emoji is returned first`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(heartEyesEmoji, heartEmoji),
            query = "heart"
        )

        assertEquals(0, result.indexOf(heartEmoji))
        assertEquals(1, result.indexOf(heartEyesEmoji))
    }

    @Test
    fun `test GIVEN two emojis WHEN sorting THEN the result should first show the higher keywordIndex match, even it has lower score`() {
        val result = EmojiSearchUtil.filterAndSortEmojis(
            emojis = listOf(blueHeartEmoji, heartCatEyes),
            query = "heart"
        )

        assertEquals(0, result.indexOf(heartCatEyes))
        assertEquals(1, result.indexOf(blueHeartEmoji))
    }


}