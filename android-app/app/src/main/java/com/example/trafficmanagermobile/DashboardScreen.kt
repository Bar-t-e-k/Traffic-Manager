package com.example.trafficmanagermobile

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TrafficViewModel) {
    var inputText by remember { mutableStateOf("101,HIGH,500,Payload") }
    val context = LocalContext.current

    val logs by viewModel.logs.collectAsState()
    val totalProcessed by viewModel.totalProcessed.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()
    val filterCriticalOnly by viewModel.filterCriticalOnly.collectAsState()

    val displayLogs = if (filterCriticalOnly) logs.filter { it.isCritical || it.isError } else logs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traffic Manager L3", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = {
                        val logText = logs.joinToString("\n") { "[#${it.id}] ${it.message}" }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Logi NDK:\n\n$logText")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Eksportuj raport"))
                    }) { Icon(Icons.Default.Share, contentDescription = "Udostępnij") }
                    IconButton(onClick = { viewModel.clearHistory() }) { Icon(Icons.Default.Delete, contentDescription = "Wyczyść") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Przetworzone", totalProcessed.toString(), Modifier.weight(1f), Color(0xFF4CAF50))
                StatCard("Krytyczne", criticalCount.toString(), Modifier.weight(1f), Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = inputText, onValueChange = { inputText = it },
                label = { Text("Wprowadź pakiet (ID,PRIO,SIZE,DATA)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.processPacket(inputText) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Parsuj")
                }
                OutlinedButton(onClick = { inputText = "999,CRITICAL,0,BOMB" }, modifier = Modifier.weight(1f)) {
                    Text("Pakiet \n(rozmiar = 0)", textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Historia Logów", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Tylko CRITICAL", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = filterCriticalOnly, onCheckedChange = { viewModel.toggleFilter() })
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(displayLogs) { log -> LogItemCard(log) }
            }
        }
    }
}