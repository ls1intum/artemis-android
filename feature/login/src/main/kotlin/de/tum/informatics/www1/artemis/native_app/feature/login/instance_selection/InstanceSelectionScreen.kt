package de.tum.informatics.www1.artemis.native_app.feature.login.instance_selection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.ArtemisHeader

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f)
        )

        ArtemisHeader(modifier = Modifier.fillMaxWidth())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f)
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.account_select_artemis_instance_select_text),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        val windowSizeClass = getWindowSizeClass()
        val columnCount = when {
            windowSizeClass.widthSizeClass > WindowWidthSizeClass.Expanded -> 6
            windowSizeClass.widthSizeClass > WindowWidthSizeClass.Medium -> 4
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(availableInstances) { instance ->
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val imageModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)

            when (item) {
                is GridCellItem.ArtemisInstanceGridCellItem -> {
                    val model = remember {
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

            val name = item.getName()

            var threeLineName: String by remember(name) {
                mutableStateOf(name)
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = threeLineName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                onTextLayout = {
                    if (it.lineCount < 3) {
                        val additionalLines = 3 - it.lineCount
                        threeLineName += "\n".repeat(additionalLines)
                    }
                }
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