package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import kotlinx.coroutines.flow.MutableStateFlow

val LocalArtemisContextProvider: ProvidableCompositionLocal<ArtemisContextProvider> = compositionLocalOf {
    EmptyArtemisContextProvider
}

private object EmptyArtemisContextProvider : ArtemisContextProvider {
    override val stateFlow = MutableStateFlow(ArtemisContext.Empty)
}

@Composable
fun ArtemisContextProvider.collectArtemisContextAsState() = this.stateFlow.collectAsState()
