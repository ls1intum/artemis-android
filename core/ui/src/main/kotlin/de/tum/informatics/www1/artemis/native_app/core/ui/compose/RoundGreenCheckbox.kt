package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

@Composable
fun RoundGreenCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (checked) ComponentColors.RoundGreenCheckbox.checkedBackground else ComponentColors.RoundGreenCheckbox.uncheckedBackground)
            .border(
                width = 2.dp,
                color = if (checked) ComponentColors.RoundGreenCheckbox.checkedBackground else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ComponentColors.RoundGreenCheckbox.checkmark,
            )
        }
    }
}
