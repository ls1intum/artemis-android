package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = text
        )
    }
}