package com.example.networkmonitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkConnectivityObserver(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val connectivityCheckUrl = "https://www.google.com/generate_204"

    private fun observe(): Flow<Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { send(Status.Available) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                launch { send(Status.Losing) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { send(Status.Lost) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { send(Status.Unavailable) }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun checkActualConnectivity(): Flow<Status> = flow {
        while (true) {
            val status = withContext(Dispatchers.IO) {
                    try {
                        val connection = URL(connectivityCheckUrl).openConnection() as HttpURLConnection
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.requestMethod = "HEAD"
                        val responseCode = connection.responseCode
                        if (responseCode == 204) {
                            return@withContext Status.Available
                        }
                    } catch (e: IOException) {
                        // Connection failed,
                    }
                Status.Unavailable
            }
            emit(status)
            delay(10000)
        }
    }

    fun combinedConnectivityStatus(): Flow<Status> {
        return combine(
            observe(),
            checkActualConnectivity()
        ) { callbackStatus, liveStatus ->
            if (callbackStatus == Status.Available && liveStatus == Status.Available) {
                Status.Available
            } else if (callbackStatus == Status.Losing) {
                Status.Losing
            } else if (callbackStatus == Status.Lost) {
                Status.Lost
            } else {
                Status.Unavailable
            }
        }.distinctUntilChanged()
    }

    sealed class Status {
        data object Available : Status()
        data object Losing : Status()
        data object Lost : Status()
        data object Unavailable: Status()
    }
}