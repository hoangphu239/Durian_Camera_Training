package com.netsservices.dct.presentation.helper.connection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


class NetworkServiceImpl @Inject constructor(context: Context) : NetworkService {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val networkStatus: Flow<NetworkService.Status> = callbackFlow {
        val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { trySend(NetworkService.Status.Available) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { trySend(NetworkService.Status.Unavailable) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { trySend (NetworkService.Status.Lost) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { trySend(NetworkService.Status.Losing) }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(connectivityCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    @SuppressLint("ObsoleteSdkInt")
    override fun isInternetAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (capabilities != null) {
                return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) true
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) true
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) true
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) true
                else false
            }
        } else {
            try {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            } catch (e: NullPointerException) {
                return false
            }
        }
        return false
    }
}