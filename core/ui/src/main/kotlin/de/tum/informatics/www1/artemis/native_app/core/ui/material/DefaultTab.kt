package de.tum.informatics.www1.artemis.native_app.core.ui.material

import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun DefaultTab(
    index: Int,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    textRes: Int,
    selectedTabIndex: Int,
    updateSelectedTabIndex: (Int) -> Unit
) {
    require(painter == null || imageVector == null) { "Only painter OR imageVector should be set" }

    Tab(
        selected = selectedTabIndex == index,
        onClick = { updateSelectedTabIndex(index) },
        icon = {
            imageVector?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                return@Tab
            }
            painter?.let { icon ->
                Icon(
                    painter = icon,
                    contentDescription = null
                )
            }
        },
        text = {
            Text(
                text = stringResource(id = textRes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
fun DefaultTab(
    index: Int,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    textRes: Int,
    selectedTabIndex: MutableState<Int>
) {
    DefaultTab(
        index = index,
        imageVector = imageVector,
        painter = painter,
        textRes = textRes,
        selectedTabIndex = selectedTabIndex.value,
        updateSelectedTabIndex = { selectedTabIndex.value = it }
    )
}