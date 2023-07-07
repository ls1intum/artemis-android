package de.tum.informatics.www1.artemis.native_app.core.ui

import android.app.Activity
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

val LocalWindowSizeClassProvider = compositionLocalOf<WindowSizeClassProvider> {
    object : WindowSizeClassProvider {
        @Composable
        override fun provideWindowSizeClass(): WindowSizeClass {
            return remember { WindowSizeClass.calculateFromSize(DpSize(200.dp, 200.dp))}
        }
    }
}

@Composable
fun getWindowSizeClass(): WindowSizeClass = LocalWindowSizeClassProvider.current.provideWindowSizeClass()

interface WindowSizeClassProvider {
    @Composable
    fun provideWindowSizeClass(): WindowSizeClass
}
