package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation

import androidx.annotation.StringRes
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
        isError = isIllegal,
        readOnly = readOnly
    )
}