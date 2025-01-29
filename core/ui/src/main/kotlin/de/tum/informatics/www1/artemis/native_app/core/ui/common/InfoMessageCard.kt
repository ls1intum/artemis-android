package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

@Composable
fun InfoMessageCard(
    modifier: Modifier = Modifier,
    infoText: String,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = ComponentColors.InfoMessageCard.border,
                shape = MaterialTheme.shapes.extraSmall
            )
            .background(ComponentColors.InfoMessageCard.background)
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
                tint = ComponentColors.InfoMessageCard.text
            )
            Text(
                text = infoText,
                fontSize = 16.sp,
                color = ComponentColors.InfoMessageCard.text
            )
        }
    }
}