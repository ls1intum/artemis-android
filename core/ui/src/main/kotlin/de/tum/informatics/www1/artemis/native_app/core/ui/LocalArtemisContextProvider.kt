package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import kotlinx.coroutines.flow.MutableStateFlow

val LocalArtemisContextProvider: ProvidableCompositionLocal<ArtemisContextProvider> = compositionLocalOf {
    EmptyArtemisContextProvider
}

private object EmptyArtemisContextProvider : ArtemisContextProvider {
    override val stateFlow = MutableStateFlow(ArtemisContextImpl.Empty)

    override fun setCourseId(courseId: Long) {}
    override fun clearCourseId() {}
}

@Composable
fun ArtemisContextProvider.collectArtemisContextAsState() = this.stateFlow.collectAsState()
