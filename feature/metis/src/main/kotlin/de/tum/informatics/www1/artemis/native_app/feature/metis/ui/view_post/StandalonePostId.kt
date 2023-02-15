package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post

internal sealed interface StandalonePostId {
    data class ClientSideId(val clientSideId: String) : StandalonePostId
    data class ServerSideId(val serverSidePostId: Long) : StandalonePostId
}