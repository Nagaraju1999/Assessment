package com.nagaraju.stocktracker.core.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes network connectivity and exposes it as a [Flow] of [Boolean].
 *
 * Repositories collect [isConnected] to decide whether to fetch from the
 * network or serve cached data. [distinctUntilChanged] prevents duplicate
 * emissions when the device oscillates between two connected networks.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Emits `true` when any validated internet-capable network is available,
     * `false` when all such networks are lost.
     *
     * The flow is cold — it only registers the system callback while it has
     * at least one active collector, and unregisters it when the last
     * collector cancels.
     */
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Only emit false if there are truly no other connected networks.
                trySend(isCurrentlyConnected())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val hasInternet = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(hasInternet && isValidated)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Emit the current state immediately so collectors don't have to wait
        // for the first connectivity change.
        trySend(isCurrentlyConnected())

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities  = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
