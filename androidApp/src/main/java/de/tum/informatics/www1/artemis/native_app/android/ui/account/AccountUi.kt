package de.tum.informatics.www1.artemis.native_app.android.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import org.koin.androidx.compose.getViewModel

/**
 * Displays the screen to login and register. Also allows to change the artemis instance.
 */
@Composable
fun AccountUi(modifier: Modifier, viewModel: AccountViewModel = getViewModel()) {
    val artemisInstance by viewModel.selectedArtemisInstance
        .collectAsState(initial = ArtemisInstances.TUM_ARTEMIS)

    val serverProfileInfo by viewModel.serverProfileInfo.collectAsState(initial = DataState.Suspended())

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ArtemisInstanceSelection(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .widthIn(400.dp),
                artemisInstance = artemisInstance,
                changeUrl = {

                }
            )

            when(serverProfileInfo) {
                is DataState.Failure -> TODO()
                is DataState.Loading -> TODO()
                is DataState.Success -> TODO()
                is DataState.Suspended -> TODO()
            }
        }
    }
}

@Composable
fun ArtemisInstanceSelection(
    modifier: Modifier,
    artemisInstance: ArtemisInstances.ArtemisInstance,
    changeUrl: (String) -> Unit
) {
    var dropdownMenuDisplayed: Boolean by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            ArtemisInstance(
                modifier = Modifier.weight(1f),
                name = stringResource(id = artemisInstance.name),
                icon = null
            )

            IconButton(
                modifier = Modifier.aspectRatio(1f),
                onClick = { dropdownMenuDisplayed = true }
            ) {
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    }

    DropdownMenu(
        modifier = Modifier.fillMaxWidth(),
        expanded = dropdownMenuDisplayed,
        onDismissRequest = { dropdownMenuDisplayed = false }) {
        ArtemisInstances.instances.forEach { instance ->
            DropdownMenuItem(
                text = {
                    ArtemisInstance(
                        modifier = Modifier.fillMaxWidth(),
                        name = stringResource(id = instance.name),
                        icon = null
                    )
                }, onClick = {
                    changeUrl(instance.serverUrl)
                }
            )
        }
    }
}

@Composable
private fun ArtemisInstance(modifier: Modifier, name: String, icon: Painter?) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            if (icon != null) {
                Image(painter = icon, contentDescription = null)
            }
        }

        Text(
            modifier = Modifier.weight(9f),
            text = name,
            fontSize = 20.sp
        )
    }
}