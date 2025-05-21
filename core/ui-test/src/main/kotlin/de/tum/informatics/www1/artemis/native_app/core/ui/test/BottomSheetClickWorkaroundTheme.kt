package de.tum.informatics.www1.artemis.native_app.core.ui.test

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// There is a bug in robolectric that makes content within a composable with rounded corners not clickable anymore.
// This is a workaround to make tests affected by this pass again.
// From: https://github.com/robolectric/robolectric/issues/9595#issuecomment-2851919271
@Composable
fun BottomSheetClickWorkaroundTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        shapes = Shapes(
            extraSmall = RoundedCornerShape(0.dp),
            small = RoundedCornerShape(0.dp),
            medium = RoundedCornerShape(0.dp),
            large = RoundedCornerShape(0.dp),
            extraLarge = RoundedCornerShape(0.dp)
        ),
        content = content,
    )
}