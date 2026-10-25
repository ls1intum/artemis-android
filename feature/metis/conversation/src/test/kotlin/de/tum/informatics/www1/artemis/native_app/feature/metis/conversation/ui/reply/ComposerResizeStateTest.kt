package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ComposerResizeStateTest {
    @Test fun `drag translation is relative to its start rather than accumulated`() {
        val first = ComposerResizeState().update(-80f, 240f)
        assertEquals(80f, first.extraHeight)
        assertEquals(120f, first.update(-120f, 240f).extraHeight)
    }

    @Test fun `drag clamps at both available height boundaries`() {
        val first = ComposerResizeState().update(-900f, 240f)
        assertEquals(240f, first.extraHeight)
        assertEquals(0f, first.update(900f, 240f).extraHeight)
    }

    @Test fun `vertical release settles in its direction`() {
        assertEquals(240f, ComposerResizeState().update(-24f, 240f).finish(-24f, maximum = 240f).extraHeight)
        val expanded = ComposerResizeState().setExpanded(true, 240f)
        assertEquals(0f, expanded.update(24f, 240f).finish(24f, maximum = 240f).extraHeight)
    }

    @Test fun `short drags return to the nearest size`() {
        assertEquals(0f, ComposerResizeState().update(-10f, 240f).finish(-10f, maximum = 240f).extraHeight)
        val expanded = ComposerResizeState().setExpanded(true, 240f)
        assertEquals(240f, expanded.update(20f, 240f).finish(20f, maximum = 240f).extraHeight)
    }

    @Test fun `horizontal release does not switch the settled size`() {
        assertEquals(0f, ComposerResizeState().finish(-25f, 100f, 240f).extraHeight)
        assertEquals(240f, ComposerResizeState().setExpanded(true, 240f).finish(25f, 100f, 240f).extraHeight)
    }

    @Test fun `interruption resets the next gesture origin to the visible height`() {
        val interrupted = ComposerResizeState().update(-100f, 240f).cancelGesture()
        assertEquals(120f, interrupted.update(-20f, 240f).extraHeight)
    }

    @Test fun `new drag starts at rendered height while a snap is interrupted`() {
        val snapping = ComposerResizeState().setExpanded(true, 240f)
        val visible = snapping.beginGesture(60f, 240f)
        assertEquals(40f, visible.update(20f, 240f).extraHeight)
    }

    @Test fun `downward drag recovers after the available height shrinks`() {
        val expanded = ComposerResizeState().setExpanded(true, 240f)
        val smaller = expanded.update(90f, 120f)
        assertEquals(30f, smaller.extraHeight)
        assertEquals(0f, smaller.finish(90f, maximum = 120f).extraHeight)
    }

    @Test fun `no expansion space remains a valid collapsed size`() {
        assertEquals(0f, ComposerResizeState().setExpanded(true, 0f).extraHeight)
        assertEquals(0f, ComposerResizeState().update(-100f, 0f).finish(-100f, maximum = 0f).extraHeight)
    }
}
