package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.impl.updatePosts
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MetisStandalonePostViewModel(
    private val clientSidePostId: String,
    subscribeToLiveUpdateService: Boolean,
    serverConfigurationService: ServerConfigurationService,
    metisService: MetisService,
    metisStorageService: MetisStorageService
) : ViewModel() {

    private val metisContext: Flow<MetisContext> = flow {
        emit(metisStorageService.getStandalonePostMetisContext(clientSidePostId))
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    val post: Flow<Post?> =
        metisStorageService
            .getStandalonePost(clientSidePostId)
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        if (subscribeToLiveUpdateService) {
            viewModelScope.launch {
                combine(serverConfigurationService.host, metisContext) { a, b -> a to b }
                    .collectLatest { (host, metisContext) ->
                        updatePosts(host, metisService, metisStorageService, metisContext)
                    }
            }
        }
    }
}