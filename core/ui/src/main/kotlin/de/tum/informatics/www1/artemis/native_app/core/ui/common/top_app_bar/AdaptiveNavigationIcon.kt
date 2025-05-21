package de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.R.drawable.sidebar_icon
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout

@Composable
fun AdaptiveNavigationIcon(
    onSidebarToggle: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val layout = getArtemisAppLayout()

    if (layout == ArtemisAppLayout.Tablet) {
        IconButton(onClick = onSidebarToggle) {
            Icon(
                painter = painterResource(id = sidebar_icon),
                contentDescription = null
            )
        }
    } else {
        if (onNavigateBack != null) {
            NavigationBackButton(onNavigateBack = onNavigateBack)
        } else {
            NavigationBackButton()
        }
    }
}
