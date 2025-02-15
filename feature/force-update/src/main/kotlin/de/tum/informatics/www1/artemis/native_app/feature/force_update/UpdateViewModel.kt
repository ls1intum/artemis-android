package de.tum.informatics.www1.artemis.native_app.feature.force_update

import androidx.lifecycle.ViewModel

class UpdateViewModel(
    private val openPlayStore: () -> Unit
) : ViewModel() {

    fun onDownloadClick() {
        openPlayStore()
    }
}
