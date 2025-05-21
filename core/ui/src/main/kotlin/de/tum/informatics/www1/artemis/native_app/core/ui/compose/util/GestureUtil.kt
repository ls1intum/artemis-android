package de.tum.informatics.www1.artemis.native_app.core.ui.compose.util

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlin.math.PI
import kotlin.math.abs

/**
 * This is a custom implementation of the PointerInputScope.detectTransformGestures function.
 * Differently from the original, this function does not consume the events if the scale is equal to 1.
 * It is used in ArtemisImageView to allow dismissing the modal bottom sheet while providing zooming and panning.
 *
 * The original function is defined here:
 * https://github.com/androidx/androidx/blob/e8c25480c465ee74155f355401010b1a330e5145/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/gestures/TransformGestureDetector.kt
 */
suspend fun PointerInputScope.detectTransformGesturesIfZoomed(
    scale: () -> Float,
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.fastAny { it.isConsumed }

                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f || zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }

                    if (scale() > 1f) {
                        event.changes.fastForEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
        }
    }
}
