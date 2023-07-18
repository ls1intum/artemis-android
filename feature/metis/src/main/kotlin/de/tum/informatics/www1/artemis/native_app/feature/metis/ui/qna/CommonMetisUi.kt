package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass

/**
 * If it is feasible to display metis on the side next to the main content of the screen.
 * @param parentWidth the maximum width of the parent in which the metis ui should be displayed
 * @param metisContentRatio the percentage of the screen width associated with metis
 */
@Composable
fun canDisplayMetisOnDisplaySide(
    windowSizeClass: WindowSizeClass = getWindowSizeClass(),
    parentWidth: Dp,
    metisContentRatio: Float
): Boolean {
    return (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
            && (parentWidth * metisContentRatio) >= 300.dp)
}