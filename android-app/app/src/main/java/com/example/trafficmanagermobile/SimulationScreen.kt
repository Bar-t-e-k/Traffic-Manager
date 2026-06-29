package com.example.trafficmanagermobile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SimulationScreen(viewModel: TrafficViewModel) {
    val isListening by viewModel.isListening.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val port by viewModel.udpPort.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("UDP Network Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("UDP Packets per Second (PPS)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth().height(250.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) { RealTimeLineChart(data = chartData) }
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { viewModel.updateUdpPort(it) },
            label = { Text("Port (default 8080)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isListening
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.toggleLiveTraffic() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isListening) "STOP LISTENING" else "START LISTENING", fontWeight = FontWeight.Bold)
        }
    }
}