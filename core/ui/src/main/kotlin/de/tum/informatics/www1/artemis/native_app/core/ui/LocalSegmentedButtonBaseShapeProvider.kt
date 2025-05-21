package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

// There is a bug in robolectric that makes content within a composable with rounded corners not clickable anymore.
// See: https://github.com/robolectric/robolectric/issues/9595#issuecomment-2851919271
// This issue also applies to SegmentedButtons, but they do no use the MaterialTheme shapes, so we can not use the
// BottomSheetClickWorkaroundTheme workaround.

val LocalSegmentedButtonBaseShapeProvider = compositionLocalOf<SegmentedButtonBaseShapeProvider> {
    DefaultSegmentedButtonBaseShapeProvider
}

interface SegmentedButtonBaseShapeProvider {
    @Composable
    fun getBaseShape(): CornerBasedShape
}

object DefaultSegmentedButtonBaseShapeProvider : SegmentedButtonBaseShapeProvider {
    @Composable
    override fun getBaseShape(): CornerBasedShape = SegmentedButtonDefaults.baseShape
}

object TestSegmentedButtonBaseShapeProvider : SegmentedButtonBaseShapeProvider {
    @Composable
    override fun getBaseShape(): CornerBasedShape = SegmentedButtonDefaults.baseShape.copy(
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp),
    )
}



