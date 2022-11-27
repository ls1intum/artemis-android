package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post

@OptIn(ExperimentalPagingApi::class)
class MetisRemoteMediator(
    private val metisContext: MetisContext
) : RemoteMediator<Int, Post>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Post>): MediatorResult {
        when (loadType) {
            LoadType.REFRESH -> TODO()
            LoadType.PREPEND -> TODO()
            LoadType.APPEND -> TODO()
        }
    }
}