package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings

@Composable
fun ArtemisPopupSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = Spacings.Popup.tonalElevation,
        shadowElevation = Spacings.Popup.shadowElevation,
    ) {
        content()
    }
}