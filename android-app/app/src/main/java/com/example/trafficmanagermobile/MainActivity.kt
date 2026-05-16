package com.example.trafficmanagermobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    // Podpinamy nasz ViewModel
    private val viewModel: TrafficViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrafficAppScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun TrafficAppScreen(viewModel: TrafficViewModel) {
    // Stan pola tekstowego
    var inputText by remember { mutableStateOf("101,HIGH,500,TestPayload") }
    // Obserwujemy wynik z C++
    val resultText by viewModel.resultState.collectAsState()

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "L3 Traffic Manager",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "C++ NDK Bridge",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pole do wpisywania surowych danych
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Dane Pakietu (ID,PRIO,SIZE,PAYLOAD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk wywołujący C++
        Button(
            onClick = { viewModel.processPacket(inputText) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Parsuj w C++ NDK")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Wyświetlanie wyniku
        Text(
            text = "Wynik z silnika:",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Karta z wynikami (ładne tło)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = resultText,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}