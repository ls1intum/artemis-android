package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import kotlin.math.abs

/** A dedicated gesture target leaves text selection, scrolling and toolbar input native. */
@Composable
internal fun ComposerResizeHandle(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    maximumExpansion: Float,
    onToggle: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: (Float, Float) -> Unit,
    onDragCancel: () -> Unit,
) {
    val description = stringResource(R.string.reply_resize)
    val expandLabel = stringResource(R.string.reply_resize_expand)
    val collapseLabel = stringResource(R.string.reply_resize_collapse)
    val state = stringResource(if (expanded) R.string.reply_resize_expanded else R.string.reply_resize_collapsed)
    val density = LocalDensity.current.density
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val start by rememberUpdatedState(onDragStart)
    val drag by rememberUpdatedState(onDrag)
    val end by rememberUpdatedState(onDragEnd)
    val cancel by rememberUpdatedState(onDragCancel)
    val enabled = maximumExpansion > 0f

    Box(
        // clickable retains Compose's 48 dp minimum touch target. The parent
        // reserves the lower extension as padding outside the editable text.
        modifier = modifier.size(width = 80.dp, height = 24.dp)
            .onGloballyPositioned { coordinates = it }
            .clip(RoundedCornerShape(24.dp))
            .semantics {
                contentDescription = description
                stateDescription = state
                if (enabled) {
                    if (expanded) collapse(collapseLabel) { onCollapse(); true }
                    else expand(expandLabel) { onExpand(); true }
                }
            }
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = if (expanded) collapseLabel else expandLabel, onClick = onToggle)
            .pointerInput(enabled, density, maximumExpansion) {
                if (!enabled) return@pointerInput
                var origin = Offset.Zero
                var last = Offset.Zero
                detectVerticalDragGestures(
                    onDragStart = { position ->
                        origin = coordinates?.localToWindow(position) ?: position
                        last = origin
                        start()
                    },
                    onVerticalDrag = { change, _ ->
                        // The handle moves with the composer. Local deltas would compound
                        // that movement; window positions track only the user's finger.
                        last = coordinates?.localToWindow(change.position) ?: change.position
                        val delta = last - origin
                        if (abs(delta.y) > abs(delta.x)) drag(delta.y / density)
                        change.consume()
                    },
                    onDragEnd = { end((last.y - origin.y) / density, (last.x - origin.x) / density) },
                    onDragCancel = { cancel() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 36.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
    }
}
