package de.tum.informatics.www1.artemis.native_app.core.ui.material

import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@Composable
fun DefaultTab(
    index: Int,
    icon: ImageVector,
    textRes: Int,
    selectedTabIndex: Int,
    updateSelectedTabIndex: (Int) -> Unit
) {
    Tab(
        selected = selectedTabIndex == index,
        onClick = { updateSelectedTabIndex(index) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        text = {
            Text(text = stringResource(id = textRes))
        }
    )
}

@Composable
fun DefaultTab(
    index: Int,
    icon: ImageVector,
    textRes: Int,
    selectedTabIndex: MutableState<Int>
) {
    DefaultTab(
        index = index,
        icon = icon,
        textRes = textRes,
        selectedTabIndex = selectedTabIndex.value,
        updateSelectedTabIndex = { selectedTabIndex.value = it }
    )
}