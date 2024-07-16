package com.example.networkmonitor

import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NetworkSpeedMonitor {
    private var lastTotalRxBytes: Long = 0
    private var lastTotalTxBytes: Long = 0
    private var lastUpdateTime: Long = 0

    /*fun monitorNetworkSpeed(): Flow<NetworkSpeed> = flow {
        while (true) {
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            val currentTime = System.currentTimeMillis()

            val timeDifference = currentTime - lastUpdateTime
            val downloadSpeed = ((currentRxBytes - lastTotalRxBytes) / timeDifference).toInt()
            val uploadSpeed = ((currentTxBytes - lastTotalTxBytes) / timeDifference).toInt()

            // We'll set the initial classification here, but it will be updated in the ViewModel
            val initialClassification = SpeedClassification.GOOD

            emit(NetworkSpeed(downloadSpeed, uploadSpeed, initialClassification))

            lastTotalRxBytes = currentRxBytes
            lastTotalTxBytes = currentTxBytes
            lastUpdateTime = currentTime

            delay(2000)
        }
    }*/

    data class NetworkSpeed(
        val downloadSpeed: Int, // in KB/s
        val uploadSpeed: Int, // in KB/s
        val classification: SpeedClassification
    )
}