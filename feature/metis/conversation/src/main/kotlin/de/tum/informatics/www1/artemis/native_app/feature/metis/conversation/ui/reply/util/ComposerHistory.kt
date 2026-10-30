package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

/** Undo stores the source document, including marks and attachment/reference payloads. */
internal class ComposerHistory {
    private val past = ArrayDeque<TextFieldValue>()
    private val future = ArrayDeque<TextFieldValue>()
    private var current: TextFieldValue? = null
    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set
    fun observe(value: TextFieldValue) {
        val old = current
        if (old != null && old.text != value.text) {
            past.addLast(old.copy(composition = null))
            if (past.size > 100) past.removeFirst()
            future.clear()
        }
        current = value
        canUndo = past.isNotEmpty(); canRedo = future.isNotEmpty()
    }
    fun commit(before: TextFieldValue, after: TextFieldValue) { observe(before); observe(after) }
    fun undo(): TextFieldValue? {
        val old = current ?: return null
        if (past.isEmpty()) return null
        future.addLast(old.copy(composition = null))
        return past.removeLast().also { current = it; canUndo = past.isNotEmpty(); canRedo = true }
    }
    fun redo(): TextFieldValue? {
        val old = current ?: return null
        if (future.isEmpty()) return null
        past.addLast(old.copy(composition = null))
        return future.removeLast().also { current = it; canUndo = true; canRedo = future.isNotEmpty() }
    }
}
