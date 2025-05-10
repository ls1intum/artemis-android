package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.CourseColors

@Composable
fun Modifier.selectionBorder(selected: Boolean): Modifier {
    return if (selected) {
        this.border(
            width = 2.dp,
            color = CourseColors.artemisDefaultColor,
            shape = MaterialTheme.shapes.medium
        )
    } else {
        this
    }
} 