package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class ReloadableViewModel : ViewModel() {

    protected val requestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    open fun onRequestReload() {
        requestReload.tryEmit(Unit)
    }
}
