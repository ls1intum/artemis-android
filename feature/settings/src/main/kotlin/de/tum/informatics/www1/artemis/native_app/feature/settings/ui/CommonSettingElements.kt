package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.settings.R

@Composable
fun PreferenceEntry(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    valueText: String? = null,
    onClick: () -> Unit,
) = PreferenceEntry(
    modifier = modifier,
    text = text,
    leadingContent = {
        Icon(icon, contentDescription = null)
    },
    valueText = valueText,
    onClick = onClick
)

@Composable
fun PreferenceEntry(
    modifier: Modifier = Modifier,
    text: String,
    leadingContent: @Composable (() -> Unit)? = null,
    valueText: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            leadingContent?.invoke()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        valueText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

        }
    }
}

@Composable
fun LogoutButtonEntry(
    modifier: Modifier = Modifier,
    onRequestLogout: () -> Unit
) {
    ButtonEntry(
        modifier = modifier,
        text = stringResource(id = R.string.settings_account_logout),
        isFocused = true,
        textColor = MaterialTheme.colorScheme.error,
        onClick = onRequestLogout
    )
}

@Composable
fun ButtonEntry(
    modifier: Modifier = Modifier,
    text: String,
    isFocused: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    ButtonEntry(
        modifier = modifier,
        isFocused = isFocused,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = textColor,
            style = if (isFocused) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ButtonEntry(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = if (isFocused) Arrangement.Center else Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()

        if (!isFocused) {
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

