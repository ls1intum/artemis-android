package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.LinkPreviewUtil
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class LinkPreviewTest {

    @Test
    fun `test GIVEN text with two links WHEN generating previewable links THEN it extracts all valid links`() {
        val text = "This is a markdown sample test containing a formatted [Link](https://example.com) and " +
                " a normal link http://test.com which should be extracted."
        val result = LinkPreviewUtil.generatePreviewableLinks(text)

        assertEquals(2, result.size)
        assertEquals("https://example.com", result[0].value)
        assertEquals("http://test.com", result[1].value)
        assertFalse(result[0].isLinkPreviewRemoved ?: true)
        assertFalse(result[1].isLinkPreviewRemoved ?: true)
    }

    @Test
    fun `test GIVEN text with removed link previews WHEN generating previewable links THEN it ignores them`() {
        val text = "This is a markdown sample test containing a formatted [Link](<https://example.com>) and " +
                " a normal link <http://test.com> which should NOT be extracted."
        val result = LinkPreviewUtil.generatePreviewableLinks(text)

        assert(result.isEmpty())
    }

    @Test
    fun `test GIVEN text with a link WHEN removing link preview THEN it correctly removes the preview`() {
        val text = "This link https://example.com/something should be removed."
        val modifiedText = LinkPreviewUtil.removeLinkPreview(text, "https://example.com/something")

        assertEquals(modifiedText, "This link <https://example.com/something> should be removed.")
    }

    @Test
    fun `test GIVEN text with multiple links WHEN removing a specific link preview THEN only the matching one is removed`() {
        val text = "This is a link that should be removed https://example.com while this link" +
                " should not be removed https://other.com."
        val modifiedText = LinkPreviewUtil.removeLinkPreview(text, "https://example.com")

        assertEquals(modifiedText, "This is a link that should be removed <https://example.com> while this link" +
                " should not be removed https://other.com.")
    }

    @Test
    fun `test GIVEN text with a url that does not end on a slash WHEN the removing the preview THEN the slash is removed to fit that url`() {
        // This would be the url that was received from the API and stored in the link preview object
        // We test if removeLinkPreview correctly removes the '/' at the end of our preview url (urlToSearchFor) to edit the text.
        val urlToSearchFor = "https://example.com/"
        val text = "This link https://example.com should be removed."
        val modifiedText = LinkPreviewUtil.removeLinkPreview(text, urlToSearchFor)

        assertEquals(modifiedText, "This link <https://example.com> should be removed.")
    }
}