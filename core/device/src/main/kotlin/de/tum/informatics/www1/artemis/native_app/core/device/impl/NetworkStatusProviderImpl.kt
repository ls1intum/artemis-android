package de.tum.informatics.www1.artemis.native_app.core.device.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkSpecifier
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class NetworkStatusProviderImpl(context: Context) : NetworkStatusProvider {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @OptIn(DelicateCoroutinesApi::class)
    override val currentNetworkStatus: Flow<NetworkStatusProvider.NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatusProvider.NetworkStatus.Internet)
            }

            override fun onUnavailable() {
                trySend(NetworkStatusProvider.NetworkStatus.Unavailable)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(NetworkStatusProvider.NetworkStatus.Unavailable)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatusProvider.NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .shareIn(GlobalScope, SharingStarted.Eagerly, replay = 1)
}