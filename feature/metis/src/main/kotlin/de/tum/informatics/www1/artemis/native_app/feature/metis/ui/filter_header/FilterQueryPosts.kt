package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.filter_header

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
internal fun FilterQueryPosts(
    modifier: Modifier,
    query: String,
    onUpdateQuery: (String) -> Unit,
    onClose: () -> Unit
) {
    var alreadyRequestedFocus by rememberSaveable {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (!alreadyRequestedFocus) {
            alreadyRequestedFocus = true
            focusRequester.requestFocus()
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            value = query,
            onValueChange = onUpdateQuery,
        )

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null
            )
        }
    }
}