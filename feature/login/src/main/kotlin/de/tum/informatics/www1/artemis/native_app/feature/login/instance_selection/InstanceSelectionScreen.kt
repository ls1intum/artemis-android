package de.tum.informatics.www1.artemis.native_app.feature.login.instance_selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun InstanceSelectionBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelectArtemisInstance: suspend (String) -> Unit,
    onRequestOpenCustomInstanceSelection: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val hideBottomSheet: (action: (suspend CoroutineScope.() -> Unit)?) -> Unit = { action ->
        scope.launch {
            if (action != null) {
                action()
            }
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        modifier = modifier,
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        InstanceSelectionScreen(
            modifier = Modifier
                .fillMaxSize()
                .pagePadding(),
            availableInstances = ArtemisInstances.instances,
            onSelectArtemisInstance = { serverUrl ->
                hideBottomSheet {
                    onSelectArtemisInstance(serverUrl)
                }
            },
            onRequestOpenCustomInstanceSelection = {
                hideBottomSheet {
                    onRequestOpenCustomInstanceSelection()
                }
            }
        )
    }
}

@Composable
internal fun InstanceSelectionScreen(
    modifier: Modifier,
    availableInstances: List<ArtemisInstances.ArtemisInstance>,
    onSelectArtemisInstance: (String) -> Unit,
    onRequestOpenCustomInstanceSelection: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.account_select_artemis_instance_select_text),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = Spacings.calculateEndOfPagePaddingValues()
        ) {
            items(
                count = availableInstances.size,
                key = { availableInstances[it].serverUrl }
            ) { index ->
                val instance = availableInstances[index]
                val item = GridCellItem.ArtemisInstanceGridCellItem(
                    instance = instance,
                    imageUrl = "${instance.serverUrl}public/images/logo.png"
                )
                ArtemisInstanceGridCell(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    onClick = { onSelectArtemisInstance(instance.serverUrl) }
                )
            }

            item {
                ArtemisInstanceGridCell(
                    modifier = Modifier.fillMaxWidth(),
                    item = GridCellItem.SelectCustom,
                    onClick = onRequestOpenCustomInstanceSelection
                )
            }
        }
    }
}

@Composable
private fun ArtemisInstanceGridCell(
    modifier: Modifier,
    item: GridCellItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageModifier = Modifier
                .height(64.dp)
                .aspectRatio(1f)

            when (item) {
                is GridCellItem.ArtemisInstanceGridCellItem -> {
                    val model = remember(item) {
                        ImageRequest
                            .Builder(context)
                            .data(item.imageUrl)
                            .build()
                    }

                    AsyncImage(
                        modifier = imageModifier,
                        model = model,
                        contentDescription = null
                    )
                }

                GridCellItem.SelectCustom -> {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = imageModifier
                    )
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left,
                text = item.getName(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private sealed class GridCellItem {

    @Composable
    abstract fun getName(): String

    data class ArtemisInstanceGridCellItem(
        val instance: ArtemisInstances.ArtemisInstance,
        val imageUrl: String
    ) : GridCellItem() {
        @Composable
        override fun getName(): String = stringResource(id = instance.name)
    }

    object SelectCustom : GridCellItem() {
        @Composable
        override fun getName(): String =
            stringResource(id = R.string.account_select_artemis_instance_custom_instance)
    }
}