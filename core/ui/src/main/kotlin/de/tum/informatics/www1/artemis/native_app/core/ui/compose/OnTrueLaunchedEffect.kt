package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect


@Composable
fun OnTrueLaunchedEffect(
    key: Boolean,
    onKeyIsTrueBlock: suspend () -> Unit
) {
    LaunchedEffect(key) {
        if (key) {
            onKeyIsTrueBlock()
        }
    }
}