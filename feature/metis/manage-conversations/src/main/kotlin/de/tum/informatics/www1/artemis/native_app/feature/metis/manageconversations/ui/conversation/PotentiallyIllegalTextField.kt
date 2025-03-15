package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
internal fun PotentiallyIllegalTextField(
    modifier: Modifier,
    @StringRes label: Int,
    value: String,
    @StringRes placeholder: Int,
    updateValue: (String) -> Unit,
    isIllegal: Boolean,
    @StringRes illegalStateExplanation: Int,
    @StringRes requiredSupportText: Int?,
    leadingIcon: (@Composable () -> Unit)? = null,
    readOnly: Boolean
) {
    PotentiallyIllegalTextField(
        modifier = modifier,
        label = stringResource(label),
        value = value,
        placeholder = stringResource(placeholder),
        updateValue = updateValue,
        isIllegal = isIllegal,
        illegalStateExplanation = stringResource(illegalStateExplanation),
        leadingIcon = leadingIcon,
        requiredSupportText = requiredSupportText?.let { stringResource(requiredSupportText) },
        readOnly = readOnly
    )
}

@Composable
internal fun PotentiallyIllegalTextField(
    modifier: Modifier,
    label: String,
    value: String,
    placeholder: String,
    updateValue: (String) -> Unit,
    isIllegal: Boolean,
    illegalStateExplanation: String,
    requiredSupportText: String?,
    leadingIcon: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = label) },
        value = value,
        onValueChange = updateValue,
        placeholder = { Text(text = placeholder) },
        supportingText = {
            if (isIllegal) {
                Text(text = illegalStateExplanation)
            } else if (requiredSupportText != null) {
                Text(text = requiredSupportText)
            }
        },
        shape = MaterialTheme.shapes.medium,
        leadingIcon = leadingIcon,
        isError = isIllegal,
        readOnly = readOnly,
        singleLine = true,
    )
}