package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyListHint(
    modifier: Modifier,
    hint: String,
    imageVector: ImageVector? = null,
    painter: Painter? = null
) {
    require(painter == null || imageVector == null) { "Only painter OR imageVector should be set" }

    Column(
        modifier = modifier.fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconModifier = Modifier.size(64.dp)
        val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
        imageVector?.let {
            Icon(
                modifier = iconModifier,
                imageVector = it,
                tint = iconTint,
                contentDescription = null
            )
        }
        painter?.let {
            Icon(
                modifier = iconModifier,
                painter = it,
                tint = iconTint,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = hint,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}