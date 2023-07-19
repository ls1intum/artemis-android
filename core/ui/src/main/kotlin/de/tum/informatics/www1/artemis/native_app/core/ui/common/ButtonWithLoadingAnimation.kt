package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

const val TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING = "TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING"

@Composable
fun ButtonWithLoadingAnimation(
    modifier: Modifier,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled && !isLoading,
        onClick = onClick,
        colors = colors
    ) {
        Crossfade(
            targetState = isLoading,
            label = "is loading animation"
        ) { isLoadingState ->
            if (isLoadingState) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag(TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING)
                )
            } else {
                content()
            }
        }
    }
}