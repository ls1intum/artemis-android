package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import kotlin.math.abs

/** Heights and translations are in dp; every drag is relative to its own origin. */
internal data class ComposerResizeState(
    val extraHeight: Float = 0f,
    private val dragStart: Float? = null,
) {
    fun beginGesture(renderedHeight: Float, maximum: Float): ComposerResizeState {
        val start = renderedHeight.coerceIn(0f, maximum.coerceAtLeast(0f))
        return copy(extraHeight = start, dragStart = start)
    }

    fun update(translation: Float, maximum: Float): ComposerResizeState {
        val limit = maximum.coerceAtLeast(0f)
        val start = dragStart ?: extraHeight.coerceIn(0f, limit)
        return copy(extraHeight = (start - translation).coerceIn(0f, limit), dragStart = start)
    }

    fun finish(translation: Float, horizontalTranslation: Float = 0f, maximum: Float): ComposerResizeState {
        val vertical = if (abs(translation) > abs(horizontalTranslation)) translation else 0f
        val expand = if (abs(vertical) >= 24f) vertical < 0f else extraHeight > maximum / 2f
        return setExpanded(expand, maximum)
    }

    fun setExpanded(expanded: Boolean, maximum: Float) =
        ComposerResizeState(if (expanded) maximum.coerceAtLeast(0f) else 0f)

    fun cancelGesture() = copy(dragStart = null)
}
