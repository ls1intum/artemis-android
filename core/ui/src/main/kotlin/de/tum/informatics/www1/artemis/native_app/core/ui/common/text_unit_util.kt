package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A [TextUnit] that is not scaled by the font scale factor which is based on the user's system font size.
 */
val TextUnit.nonScaledSp
    @Composable
    get() = (this.value / LocalDensity.current.fontScale).sp