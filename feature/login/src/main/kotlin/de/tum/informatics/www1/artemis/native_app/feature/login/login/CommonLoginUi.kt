package de.tum.informatics.www1.artemis.native_app.feature.login.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.login.R

@Composable
fun PasswordTextField(
    modifier: Modifier,
    password: String,
    label: String,
    updatePassword: (String) -> Unit,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Done
) {
    var showPasswordPlaintext by rememberSaveable { mutableStateOf(false) }
    val visualTransformation = remember(showPasswordPlaintext) {
        if (showPasswordPlaintext) {
            VisualTransformation.None
        } else PasswordVisualTransformation()
    }

    TextField(
        modifier = modifier,
        value = password,
        onValueChange = updatePassword,
        label = { Text(text = label) },
        visualTransformation = visualTransformation,
        trailingIcon = {
            IconButton(onClick = { showPasswordPlaintext = !showPasswordPlaintext }) {
                Icon(
                    imageVector = if (!showPasswordPlaintext) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrect = false,
            imeAction = imeAction
        ),
        isError = isError
    )
}

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