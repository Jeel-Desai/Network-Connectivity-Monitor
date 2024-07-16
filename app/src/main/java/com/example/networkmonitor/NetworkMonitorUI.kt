package com.example.networkmonitor

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NetworkMonitorUI(viewModel: NetworkMonitorViewModel, paddingValues: PaddingValues) {
    val networkStatus = viewModel.networkStatus.collectAsState()
    val actualConnectivity = viewModel.actualConnectivity.collectAsState()
    val bufferPercentage = viewModel.bufferPercentage.collectAsState()
    val showWarning = viewModel.showWarning.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            VideoPlayer(
                videoUrl = "https://videos.pexels.com/video-files/3577871/3577871-hd_1920_1080_25fps.mp4",
                onBufferUpdate = { buffer, position, duration ->
                    viewModel.updateBufferInfo(buffer, position, duration)
                },
                onSeek = {
                    viewModel.onSeek()
                },
                onPlaybackReset = {
                    viewModel.resetPlaybackStart()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            if (networkStatus.value == NetworkConnectivityObserver.Status.Available && showWarning.value) {
                Text(
                    text = "Unstable Internet: For a better experience, please connect to a faster internet connection like Wi-Fi or 5G.",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Network Monitor", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Buffer Percentage: ${bufferPercentage.value}%")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = buildAnnotatedString {
                        append("Network Status: ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = getStatusColor(networkStatus.value)
                            )
                        ) {
                            append(getStatusText(networkStatus.value))
                        }
                    })
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = buildAnnotatedString {
                        append("Actual Connectivity: ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = getStatusColor(actualConnectivity.value)
                            )
                        ) {
                            append(getStatusText(actualConnectivity.value))
                        }
                    })
                }
            }
        }
    }
}


@Composable
fun getStatusColor(status: NetworkConnectivityObserver.Status): Color {
    return when (status) {
        is NetworkConnectivityObserver.Status.Available -> Color.Green
        is NetworkConnectivityObserver.Status.Unavailable -> MaterialTheme.colorScheme.error
        is NetworkConnectivityObserver.Status.Lost -> MaterialTheme.colorScheme.error
        is NetworkConnectivityObserver.Status.Losing -> MaterialTheme.colorScheme.errorContainer
    }
}

fun getStatusText(status: NetworkConnectivityObserver.Status): String {
    return when (status) {
        is NetworkConnectivityObserver.Status.Available -> "Connected"
        is NetworkConnectivityObserver.Status.Unavailable -> "Unavailable"
        is NetworkConnectivityObserver.Status.Lost -> "Lost"
        is NetworkConnectivityObserver.Status.Losing -> "Losing"
    }
}