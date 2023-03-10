package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.account.R

@Composable
internal fun RememberLoginCheckBox(
    modifier: Modifier,
    rememberMe: Boolean,
    updateRememberMe: (Boolean) -> Unit
) {
    CheckboxWithText(
        modifier = modifier,
        isChecked = rememberMe,
        text = stringResource(id = R.string.login_remember_me_label),
        onCheckedChanged = updateRememberMe
    )
}

@Composable
internal fun CheckboxWithText(
    modifier: Modifier,
    isChecked: Boolean,
    text: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(modifier = modifier) {
        Checkbox(
            modifier = Modifier,
            checked = isChecked,
            onCheckedChange = onCheckedChanged,
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = text
        )
    }
}

@Composable
internal fun DividerWithText(modifier: Modifier, text: @Composable (Modifier) -> Unit) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        text(Modifier)

        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}