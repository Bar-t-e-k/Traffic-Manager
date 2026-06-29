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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                            putExtra(Intent.EXTRA_TEXT, "Logs NDK:\n\n$logText")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export report"))
                    }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = { viewModel.clearHistory() }) { Icon(Icons.Default.Delete, contentDescription = "Clear") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Processed", totalProcessed.toString(), Modifier.weight(1f), Color(0xFF4CAF50))
                StatCard("Critical", criticalCount.toString(), Modifier.weight(1f), Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = inputText, onValueChange = { inputText = it },
                label = { Text("Enter packet (ID,PRIO,SIZE,DATA)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.processPacket(inputText) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Locally", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.sendReliablePacket(inputText) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Send (UDP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { inputText = "999,CRITICAL,0,BOMB" },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Bomb", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(displayLogs) { log -> LogItemCard(log) }
            }
        }
    }
}