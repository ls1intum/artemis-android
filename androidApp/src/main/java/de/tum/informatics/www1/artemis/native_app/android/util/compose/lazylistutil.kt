package de.tum.informatics.www1.artemis.native_app.android.util.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

/**
 * Returns whether the lazy list is currently scrolling up.
 * Source: https://github.com/googlecodelabs/android-compose-codelabs/blob/main/AnimationCodelab/finished/src/main/java/com/example/android/codelab/animation/ui/home/Home.kt#L339
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}