package de.tum.informatics.www1.artemis.native_app.core.ui.material.colors

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ComponentColors {
    object TextAlertDialog {
        val destructiveButtonFont: Color
            @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF4F4F) else Color(0xFF800000)
    }

    object InfoMessageCard {
        val background: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF062A30) else Color(0xFFD1ECF1)
        val border: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF148EA1) else Color(0xFFA2DAE3)
        val text: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF36CEE6) else Color(0xFF09414A)
    }

    object ArtemisTopAppBar {
        val background: Color
            @Composable get() = MaterialTheme.colorScheme.surfaceContainer
        val searchBarShadow: Color
            @Composable get() = if(isSystemInDarkTheme()) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.35f)
    }

    object BrowseChannelCard {
        val joinedBackground: Color
            @Composable get() = Color(0xff28a745)
        val actionText: Color
            @Composable get() = Color.White
    }

    object ChannelFilter {
        val all: Color
            get() = Color(0xFF005388)
        val unread: Color
            get() = Color(0xFF7E00D7)
        val recent: Color
            get() = Color(0xFFFD7E14)
        val unresolved: Color
            get() = Color(0xFF28A745)
    }

    object SavedMessageFilter {
        val inProgress: Color
            get() = Color(0xFF005388)
        val archive: Color
            get() = Color(0xFF717070)
        val completed: Color
            get() = Color(0xFF28A745)
    }

    object RoundGreenCheckbox {
        val checkedBackground: Color
            @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4CAF50) else Color(0xFF4CAF50)
        val checkmark: Color
            @Composable get() = Color.White
        val uncheckedBackground: Color
            @Composable get() = Color.Transparent
    }
}