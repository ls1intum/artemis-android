package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.AndroidClipboard
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.nativeClipboardManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerClipboardTest {
    private fun platformClipboard(): AndroidClipboard = object : AndroidClipboard {
        override val clipboardManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        override suspend fun getClipEntry() = clipboardManager.primaryClip?.let(::ClipEntry)
        override suspend fun setClipEntry(clipEntry: ClipEntry?) {
            if (clipEntry == null) clipboardManager.clearPrimaryClip()
            else clipboardManager.setPrimaryClip(clipEntry.clipData)
        }
    }

    @Test fun `native selection menu can inspect text clipboard through the composer bridge`() = runBlocking {
        val delegate = platformClipboard()
        delegate.setClipEntry(ClipEntry(ClipData.newPlainText("Test", "external text")))
        val source = TextFieldValue("**hello**", TextRange(2, 7))
        val bridge = ComposerClipboard(delegate) { source }
        // Compose 1.12 uses this extension when opening the native selection menu.
        assertTrue(bridge.nativeClipboardManager.hasPrimaryClip())
        assertTrue(bridge.nativeClipboardManager.primaryClipDescription!!.hasMimeType("text/*"))
        assertEquals("external text", bridge.nativeClipboardManager.primaryClip!!.getItemAt(0).text.toString())
        assertEquals("**hello**", source.text)
    }

    @Test fun `native selection menu handles an empty clipboard`() = runBlocking {
        val delegate = platformClipboard()
        delegate.setClipEntry(null)
        val bridge = ComposerClipboard(delegate) { TextFieldValue("hello") }
        assertFalse(bridge.nativeClipboardManager.hasPrimaryClip())
        assertEquals(null, bridge.nativeClipboardManager.primaryClipDescription)
        assertEquals(null, bridge.getClipEntry())
    }

    @Test fun `native clipboard availability probes do not evict a pending formatted paste`() = runBlocking {
        val delegate = platformClipboard()
        delegate.setClipEntry(ClipEntry(ClipData.newPlainText("Test", "**pasted**")))
        val source = TextFieldValue("hello ", TextRange(6))
        val bridge = ComposerClipboard(delegate) { source }
        val token = bridge.getClipEntry()!!.clipData.getItemAt(0).text.toString()
        repeat(12) {
            assertTrue(bridge.nativeClipboardManager.primaryClipDescription!!.hasMimeType("text/*"))
            assertEquals("**pasted**", bridge.nativeClipboardManager.primaryClip!!.getItemAt(0).text.toString())
        }
        val pasted = assertNotNull(bridge.consumePaste(source, TextFieldValue("hello $token"), null))
        assertEquals("hello **pasted**", pasted.text)
        assertEquals(TextRange(12), MarkdownEditEngine.visibleValue(pasted).selection)
    }

    @Test fun `copy preserves readable platform text and formatted companion for the native menu`() = runBlocking {
        val delegate = platformClipboard()
        val source = TextFieldValue("**hello**", TextRange(2, 7))
        val bridge = ComposerClipboard(delegate) { source }
        bridge.setClipEntry(ClipEntry(ClipData.newPlainText("Test", "hello")))
        val clip = bridge.nativeClipboardManager.primaryClip!!
        assertEquals("hello", clip.getItemAt(0).text.toString())
        assertTrue(clip.description.hasMimeType("text/*"))
        assertEquals("**hello**", clip.description.extras!!.getString("de.tum.artemis.composer.markdown"))
        bridge.setClipEntry(null)
        assertFalse(delegate.clipboardManager.hasPrimaryClip())
    }

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
