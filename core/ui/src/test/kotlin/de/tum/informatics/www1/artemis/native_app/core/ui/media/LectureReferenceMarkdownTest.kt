package de.tum.informatics.www1.artemis.native_app.core.ui.media

import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.PostArtemisMarkdownTransformer
import org.junit.Assert.*
import org.junit.Test

@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureReferenceMarkdownTest {
    private val transformer = PostArtemisMarkdownTransformer("https://a.test", 9)
    @Test fun modernSlideKeepsItsStableSlideIdentifier() {
        val text = transformer.transformMarkdown("[slide]Page 3(#42)[/slide]")
        assertTrue(text, text.contains("artemis://courses/9/lecture-media?referenceType=slide&reference=%2342"))
        assertFalse(text.contains("![Page 3]"))
    }
    @Test fun legacySlideKeepsUnitAndOneBasedPage() {
        val text = transformer.transformMarkdown("[slide]Page 3(attachment-unit/8/slide/3)[/slide]")
        assertTrue(text, text.contains("reference=attachment-unit%2F8%2Fslide%2F3"))
    }
    @Test fun attachmentAndUnitPathsKeepVersionAndParentheses() {
        val text = transformer.transformMarkdown("[lecture-unit]Slides(attachment-unit/8/student/intro(draft).pdf?version=3)[/lecture-unit]")
        assertTrue(text, text.contains("referenceType=lecture-unit"))
        assertTrue(text, text.contains("%3Fversion%3D3"))
        assertFalse(text, text.contains("[/lecture-unit]"))
    }
}
