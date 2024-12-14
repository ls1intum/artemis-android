package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CommunicationColors {
    private val ArtemisMainBlue = Color(0xFF3E8ACC)
    private val ArtemisLightBlue = Color(0xFFB5CEE4)

    val ArtemisBlue: Color
        @Composable
        get() = if (isSystemInDarkTheme()) ArtemisLightBlue else ArtemisMainBlue
}
