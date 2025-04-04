package de.tum.informatics.www1.artemis.native_app.core.ui.material

import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun DefaultTab(
    index: Int,
    iconPainter: Painter,
    textRes: Int,
    selectedTabIndex: Int,
    updateSelectedTabIndex: (Int) -> Unit
) {
    Tab(
        selected = selectedTabIndex == index,
        onClick = { updateSelectedTabIndex(index) },
        icon = {
            Icon(
                painter = iconPainter,
                contentDescription = null
            )
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
    iconPainter: Painter,
    textRes: Int,
    selectedTabIndex: MutableState<Int>
) {
    DefaultTab(
        index = index,
        iconPainter = iconPainter,
        textRes = textRes,
        selectedTabIndex = selectedTabIndex.value,
        updateSelectedTabIndex = { selectedTabIndex.value = it }
    )
}