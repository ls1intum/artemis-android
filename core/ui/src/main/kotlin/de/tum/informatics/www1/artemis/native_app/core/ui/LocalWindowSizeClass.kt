package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSizeClassProvider = compositionLocalOf<WindowSizeClassProvider> { error("No LocalWindowSizeClass provided") }

@Composable
fun getWindowSizeClass(): WindowSizeClass = LocalWindowSizeClassProvider.current.provideWindowSizeClass()

interface WindowSizeClassProvider {
    @Composable
    fun provideWindowSizeClass(): WindowSizeClass
}