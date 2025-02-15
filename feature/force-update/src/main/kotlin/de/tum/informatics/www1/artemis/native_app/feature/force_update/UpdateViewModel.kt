package de.tum.informatics.www1.artemis.native_app.feature.force_update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.feature.force_update.repository.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val updateRepository: UpdateRepository,
    private val openPlayStore: () -> Unit
) : ViewModel() {

    private val _updateRequired = MutableStateFlow(false)
    val updateRequired: StateFlow<Boolean> = _updateRequired.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            val result = updateRepository.checkForUpdate()
            _updateRequired.value = result.updateRequired
        }
    }

    fun onDownloadClick() {
        openPlayStore()
    }
}
