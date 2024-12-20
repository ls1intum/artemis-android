package de.tum.informatics.www1.artemis.native_app.core.ui.material.colors

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ParticipationNotPossibleInfoMessageCardColors {
    val background: Color
        @Composable get() = if(isSystemInDarkTheme()) Color(0xFF062A30) else Color(0xFFD1ECF1)
    val border: Color
        @Composable get() = if(isSystemInDarkTheme()) Color(0xFF148EA1) else Color(0xFFA2DAE3)
    val text: Color
        @Composable get() = if(isSystemInDarkTheme()) Color(0xFF36CEE6) else Color(0xFF09414A)
}