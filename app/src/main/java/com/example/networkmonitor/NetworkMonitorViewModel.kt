package com.example.networkmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkMonitorViewModel(
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _showWarning = MutableStateFlow(false)
    val showWarning = _showWarning.asStateFlow()

    private var lastSeekTime: Long = 0
    private var playbackStartTime: Long = 0
    private var isAtStart: Boolean = true
    private var lastNetworkChangeTime: Long = 0

    private val _networkStatus = MutableStateFlow<NetworkConnectivityObserver.Status>(
        NetworkConnectivityObserver.Status.Unavailable
    )
    val networkStatus = _networkStatus.asStateFlow()

    private val _actualConnectivity = MutableStateFlow<NetworkConnectivityObserver.Status>(
        NetworkConnectivityObserver.Status.Unavailable
    )
    val actualConnectivity = _actualConnectivity.asStateFlow()

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage = _bufferPercentage.asStateFlow()

    private val _speedClassification = MutableStateFlow(SpeedClassification.GOOD)
    val speedClassification = _speedClassification.asStateFlow()

    init {
        viewModelScope.launch {
            networkConnectivityObserver.combinedConnectivityStatus().collect {
                _networkStatus.value = it
                onNetworkChanged()
            }
        }

        viewModelScope.launch {
            networkConnectivityObserver.combinedConnectivityStatus().collect {
                _actualConnectivity.value = it
            }
        }

       /* viewModelScope.launch {
            networkConnectivityObserver.combinedConnectivityStatus().collect {
                _actualConnectivity.value = it
            }
        }*/

    }

    private fun onNetworkChanged() {
        lastNetworkChangeTime = System.currentTimeMillis()
        updateWarningVisibility()
    }

    private var currentPosition: Long = 0
    private var duration: Long = 0
    private val networkChangeDelayMS = 5000

    fun updateBufferInfo(bufferPercentage: Int, currentPosition: Long, duration: Long) {
        _bufferPercentage.value = bufferPercentage
        this.currentPosition = currentPosition
        this.duration = duration

        if (/*isAtStart && */currentPosition > 0) {
            isAtStart = false
            playbackStartTime = System.currentTimeMillis()
        }

        val vtp = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) * 100 else 0f
        val difference = bufferPercentage - vtp

        val newClassification = when {
            difference >= 24 -> SpeedClassification.FAST
            difference in 6.0..23.99 -> SpeedClassification.GOOD
            else -> SpeedClassification.SLOW
        }

        _speedClassification.value = newClassification

        updateWarningVisibility()
    }

    fun onSeek() {
        lastSeekTime = System.currentTimeMillis()
        updateWarningVisibility()
    }

    private fun updateWarningVisibility() {
        val currentTime = System.currentTimeMillis()
        val shouldShowWarning = when {
//            isAtStart -> false
            currentTime - playbackStartTime <= 5000 -> false
            currentTime - lastSeekTime <= 5000 -> false
            bufferPercentage.value == 100 -> false
            currentTime - lastNetworkChangeTime <= networkChangeDelayMS -> false
            _speedClassification.value == SpeedClassification.SLOW -> true
            else -> false
        }
        _showWarning.value = shouldShowWarning
    }

    fun resetPlaybackStart() {
        isAtStart = true
        playbackStartTime = 0
        updateWarningVisibility()
    }
}

enum class SpeedClassification {
    SLOW, GOOD, FAST
}