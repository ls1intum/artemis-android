package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.StandalonePostId.ClientSideId
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.StandalonePostId.ServerSideId
import kotlinx.parcelize.Parcelize

/**
 * Represent a standalone post id. Can be both a [ClientSideId] and a [ServerSideId].
 */
@Parcelize
sealed interface StandalonePostId : Parcelable {
    @Parcelize
    data class ClientSideId(val clientSideId: String) : StandalonePostId

    @Parcelize
    data class ServerSideId(val serverSidePostId: Long) : StandalonePostId
}
