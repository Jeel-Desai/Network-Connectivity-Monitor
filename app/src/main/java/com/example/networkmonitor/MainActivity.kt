package com.example.networkmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.networkmonitor.ui.theme.NetworkMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkMonitorTheme {
                Scaffold { innerPadding ->
                    val context = LocalContext.current
                    val networkConnectivityObserver = remember { NetworkConnectivityObserver(context) }
                    val viewModel = remember { NetworkMonitorViewModel(networkConnectivityObserver) }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        NetworkMonitorUI(viewModel, innerPadding)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkMonitorPreview() {
    val context = LocalContext.current
    val networkConnectivityObserver = remember { NetworkConnectivityObserver(context) }
    val viewModel = remember { NetworkMonitorViewModel(networkConnectivityObserver) }
    val innerPadding = remember { PaddingValues(16.dp) }

    NetworkMonitorTheme {
        NetworkMonitorUI(viewModel, innerPadding)
    }

}