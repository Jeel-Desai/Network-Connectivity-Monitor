package com.example.networkmonitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayer(
    videoUrl: String,
    onBufferUpdate: (Int, Long, Long) -> Unit,
    onSeek: () -> Unit,
    onPlaybackReset: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    onSeek()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && exoPlayer.currentPosition == 0L) {
                    onPlaybackReset()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxWidth().aspectRatio(5f/3f)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            onBufferUpdate(
                exoPlayer.bufferedPercentage,
                exoPlayer.currentPosition,
                exoPlayer.duration
            )
            delay(1000)
        }
    }
}


@Composable
fun NetworkConnectivityMonitor() {
    val context = LocalContext.current
    var showWarning by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Check initial network status
    LaunchedEffect(Unit) {
        checkNetworkStatus(context) { isConnected ->
            showWarning = !isConnected
        }
    }

    // Monitor network changes
    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {override fun onAvailable(network: Network) {
            if (!isChecking) {
                isChecking = true
                coroutineScope.launch {
                    delay(5000) // Wait for 5 seconds
                    checkNetworkStatus(context) { isConnected ->
                        showWarning = !isConnected
                    }
                    isChecking = false
                }
            }
        }

            override fun onLost(network: Network) {
                showWarning = true
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    // Display warning if necessary
    if (showWarning) {
        // Show your warning message here, e.g., using a Snackbar
    }
}

// Helper function to check network status
fun checkNetworkStatus(context: Context, callback: (Boolean) -> Unit) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    callback(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
}