package com.example.trafficmanagermobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val viewModel: TrafficViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: TrafficViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Terminal") },
                    label = { Text("Terminal") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Sieć UDP") },
                    label = { Text("Sieć UDP") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Generator") },
                    label = { Text("Generator") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel)
                1 -> SimulationScreen(viewModel)
                2 -> GeneratorScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(viewModel: TrafficViewModel) {
    val packetCountText by viewModel.generatorPacketCount.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val logs by viewModel.logs.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lokalny Generator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Wygeneruj określoną liczbę pakietów i przetestuj wydajność silnika.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = packetCountText,
            onValueChange = { viewModel.updateGeneratorPacketCount(it) },
            label = { Text("Ilość pakietów do wygenerowania") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isGenerating
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.toggleLocalSimulation() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGenerating) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isGenerating) "ZATRZYMAJ GENEROWANIE" else "URUCHOM STRESS TEST")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Podgląd na żywo (ostatnie 200 zdarzeń)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log -> LogItemCard(log) }
        }
    }
}

@Composable
fun SimulationScreen(viewModel: TrafficViewModel) {
    val isListening by viewModel.isListening.collectAsState()
    val chartData by viewModel.chartData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Analiza Sieci w Czasie Rzeczywistym", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pakiety UDP na sekundę (PPS)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                RealTimeLineChart(data = chartData)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.toggleLiveTraffic() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isListening) "ZATRZYMAJ NASŁUCHIWANIE (PORT 8080)" else "NASŁUCHUJ RUCH (PORT 8080)", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TrafficViewModel) {
    var inputText by remember { mutableStateOf("101,HIGH,500,Payload") }

    val logs by viewModel.logs.collectAsState()
    val totalProcessed by viewModel.totalProcessed.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traffic Manager", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Wyczyść")
                    }
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
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Wprowadź pakiet (ID,PRIO,SIZE,DATA)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.processPacket(inputText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parsuj")
                }

                OutlinedButton(
                    onClick = { inputText = "999,CRITICAL,0,BOMB" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rozmiar = 0 \n(do testów)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Historia Logów", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogItemCard(log)
                }
            }
        }
    }
}

@Composable
fun RealTimeLineChart(data: List<Float>) {
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxDataValue = data.maxOrNull()?.coerceAtLeast(10f) ?: 100f
        val distance = size.width / (data.size - 1).coerceAtLeast(1)

        val path = Path()

        data.forEachIndexed { index, value ->
            val x = index * distance
            val y = size.height - (value / maxDataValue) * size.height

            if (index == 0) {
                path.moveTo(x = x, y = y)
            } else {
                path.lineTo(x = x, y = y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun LogItemCard(log: TrafficLog) {
    val containerColor = when {
        log.isError -> MaterialTheme.colorScheme.errorContainer
        log.isCritical -> Color(0xFFFFE0B2)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp, max = 56.dp)
                    .defaultMinSize(minHeight = 32.dp)
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${log.id}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(log.message, style = MaterialTheme.typography.bodySmall)
        }
    }
}