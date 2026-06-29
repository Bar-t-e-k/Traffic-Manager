package com.example.trafficmanagermobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(viewModel: TrafficViewModel) {
    val packetCountText by viewModel.generatorPacketCount.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val filterCriticalOnly by viewModel.filterCriticalOnly.collectAsState()

    val displayLogs = if (filterCriticalOnly) logs.filter { it.isCritical || it.isError } else logs

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Local Generator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Generate a specified number of packets and test the engine's performance.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = packetCountText, onValueChange = { viewModel.updateGeneratorPacketCount(it) },
            label = { Text("Number of packets to generate") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isGenerating
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.toggleLocalSimulation() }, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isGenerating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null); Spacer(modifier = Modifier.width(8.dp))
            Text(if (isGenerating) "STOP GENERATION" else "START STRESS TEST")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Preview (max 200 entries)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Critical Only", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = filterCriticalOnly, onCheckedChange = { viewModel.toggleFilter() })
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(displayLogs) { log -> LogItemCard(log) }
        }
    }
}