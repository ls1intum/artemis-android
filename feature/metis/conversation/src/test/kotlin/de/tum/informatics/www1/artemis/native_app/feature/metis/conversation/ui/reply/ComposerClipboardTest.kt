package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerClipboardTest {
    private class NativeClipboard : Clipboard {
        var entry: ClipEntry? = ClipEntry(ClipData.newPlainText("Test", "**pasted**"))
        override suspend fun getClipEntry() = entry
        override suspend fun setClipEntry(clipEntry: ClipEntry?) { entry = clipEntry }
    }

    @Test fun `selection changes before delayed paste cancel without exposing its token`() = runBlocking {
        var source = TextFieldValue("draft", TextRange(5))
        val bridge = ComposerClipboard(NativeClipboard()) { source }
        val token = bridge.getClipEntry()!!.clipData.getItemAt(0).text.toString()
        bridge.consumePaste(source, source.copy(selection = TextRange(0)), null)
        source = source.copy(selection = TextRange(0))
        val result = bridge.consumePaste(source, TextFieldValue("draft$token"), null)
        assertNotNull(result)
        assertEquals(source, result)
    }

    @Test fun `evicted clipboard probes cannot leak a retired insertion token`() = runBlocking {
        val source = TextFieldValue()
        val bridge = ComposerClipboard(NativeClipboard()) { source }
        val token = bridge.getClipEntry()!!.clipData.getItemAt(0).text.toString()
        repeat(9) { bridge.getClipEntry() }
        val result = bridge.consumePaste(source, TextFieldValue(token), null)
        assertNotNull(result)
        assertEquals(source, result)
    }

    @Test fun `duplicate paste delivery cannot insert its already consumed token`() = runBlocking {
        var source = TextFieldValue()
        val bridge = ComposerClipboard(NativeClipboard()) { source }
        val token = bridge.getClipEntry()!!.clipData.getItemAt(0).text.toString()
        source = assertNotNull(bridge.consumePaste(source, TextFieldValue(token), null))
        val result = bridge.consumePaste(source, TextFieldValue("pasted$token"), null)
        assertNotNull(result)
        assertEquals(source, result)
    }
}
