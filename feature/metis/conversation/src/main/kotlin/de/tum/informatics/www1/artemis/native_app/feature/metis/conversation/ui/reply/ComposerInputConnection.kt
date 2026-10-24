package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.platform.PlatformTextInputMethodRequest

/** IMEs can delete at offset zero without producing a text change; remove the block at that boundary. */
internal class ComposerInputConnection(target: InputConnection, private val onBackspace: () -> Boolean) : InputConnectionWrapper(target, false) {
    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean =
        if (beforeLength == 1 && afterLength == 0 && onBackspace()) true else super.deleteSurroundingText(beforeLength, afterLength)

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean =
        if (beforeLength == 1 && afterLength == 0 && onBackspace()) true else super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)

    override fun sendKeyEvent(event: KeyEvent): Boolean =
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL && onBackspace()) true else super.sendKeyEvent(event)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ComposerKeyboardInput(onBackspace: () -> Boolean, content: @Composable () -> Unit) {
    val backspace by rememberUpdatedState(onBackspace)
    val interceptor = remember {
        PlatformTextInputInterceptor { request, next ->
            next.startInputMethod(object : PlatformTextInputMethodRequest {
                override fun createInputConnection(outAttributes: EditorInfo): InputConnection =
                    ComposerInputConnection(request.createInputConnection(outAttributes)) { backspace() }
            })
        }
    }
    InterceptPlatformTextInput(interceptor, content)
}
