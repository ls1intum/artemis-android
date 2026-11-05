package de.tum.informatics.www1.artemis.native_app.core.common.markdown
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
@Category(UnitTest::class)
class ArtemisLectureMarkdownTest {
    private val transformer = object : ArtemisMarkdownTransformer() {
        override fun transformLectureContentMarkdown(type: String, fileName: String, url: String): String = "$type|$url"
    }
    @Test fun modernSlideIdIsRecognized() {
        assertEquals("slide|#42", transformer.transformMarkdown("[slide]Page 3(#42)[/slide]"))
    }
    @Test fun unitVersionAndParenthesesArePreserved() {
        assertEquals("lecture-unit|attachment-unit/8/student/intro(draft).pdf?version=3", transformer.transformMarkdown("[lecture-unit]Slides(attachment-unit/8/student/intro(draft).pdf?version=3)[/lecture-unit]"))
    }
}
