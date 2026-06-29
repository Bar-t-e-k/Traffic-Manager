package com.example.trafficmanagermobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

data class TrafficLog(
    val id: Int,
    val message: String,
    val isError: Boolean,
    val isCritical: Boolean
)

class TrafficViewModel : ViewModel() {
    private val engine = TrafficEngine()

    // STAN GŁÓWNY
    private val _logs = MutableStateFlow<List<TrafficLog>>(emptyList())
    val logs: StateFlow<List<TrafficLog>> = _logs.asStateFlow()

    private val _totalProcessed = MutableStateFlow(0)
    val totalProcessed: StateFlow<Int> = _totalProcessed.asStateFlow()

    private val _criticalCount = MutableStateFlow(0)
    val criticalCount: StateFlow<Int> = _criticalCount.asStateFlow()
    private var logCounter = 0

    private val _filterCriticalOnly = MutableStateFlow(false)
    val filterCriticalOnly: StateFlow<Boolean> = _filterCriticalOnly.asStateFlow()

    private val _udpPort = MutableStateFlow("8080")
    val udpPort: StateFlow<String> = _udpPort.asStateFlow()

    // STAN ZAKŁADKI SIECIOWEJ
    private var udpJob: Job? = null
    private var chartTimerJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _chartData = MutableStateFlow(List(20) { 0f })
    val chartData: StateFlow<List<Float>> = _chartData.asStateFlow()

    private val packetsThisSecond = AtomicInteger(0)

    // STAN ZAKŁADKI GENERATORA
    private var localGenJob: Job? = null

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatorPacketCount = MutableStateFlow("100")
    val generatorPacketCount: StateFlow<String> = _generatorPacketCount.asStateFlow()

    fun toggleFilter() {
        _filterCriticalOnly.value = !_filterCriticalOnly.value
    }

    fun updateUdpPort(newPort: String) {
        if (newPort.all { it.isDigit() } && newPort.length <= 5) {
            _udpPort.value = newPort
        }
    }

    fun updateGeneratorPacketCount(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _generatorPacketCount.value = newValue
        }
    }

    fun processPacket(rawData: String) {
        if (rawData.isBlank()) return
        logCounter++

        try {
            val result = engine.parsePacketNative(rawData)

            val isError = result.startsWith("Error", ignoreCase = true)
            val isCriticalRequest = rawData.contains("CRITICAL") && !isError

            addLog(TrafficLog(logCounter, result, isError = isError, isCritical = isCriticalRequest))

            if (!isError) {
                _totalProcessed.update { it + 1 }
                if (isCriticalRequest) _criticalCount.update { it + 1 }
            }
        } catch (e: Exception) {
            addLog(TrafficLog(logCounter, "ERROR: ${e.message}", isError = true, isCritical = false))
        }
    }

    private fun addLog(log: TrafficLog) {
        _logs.update { currentLogs ->
            (listOf(log) + currentLogs).take(200)
        }
    }

    fun clearHistory() {
        _logs.value = emptyList()
        _totalProcessed.value = 0
        _criticalCount.value = 0
        logCounter = 0
    }

    fun toggleLiveTraffic() {
        if (_isListening.value) {
            _isListening.value = false
            engine.stopRudpServerNative()
            udpJob?.cancel()
            chartTimerJob?.cancel()
        } else {
            val portToUse = _udpPort.value.toIntOrNull()?.coerceIn(1024, 65535) ?: 8080
            _udpPort.value = portToUse.toString()

            val success = engine.startRudpServerNative(portToUse)
            if (!success) {
                addLog(TrafficLog(0, "NDK Error: Failed to run RUDP Server on the port $portToUse",
                    isError = true, isCritical = false))
                return
            }

            _isListening.value = true
            packetsThisSecond.set(0)

            chartTimerJob = viewModelScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1000.milliseconds)
                    val pps = packetsThisSecond.getAndSet(0)
                    _chartData.update { currentData ->
                        (currentData + pps.toFloat()).takeLast(20)
                    }
                }
            }

            udpJob = viewModelScope.launch(Dispatchers.IO) {
                try {
                    while (isActive) {
                        val logResult = engine.receiveRudpPacketNative()

                        if (logResult.contains("Server not running")) break

                        if (!logResult.contains("Error:")) {
                            packetsThisSecond.incrementAndGet()
                        }

                        logCounter++
                        val isCritical = logResult.contains("CRITICAL")
                        val isError = logResult.contains("Error:") || logResult.contains("RUDP Parse Error")

                        if (!isError) {
                            if (isCritical) _criticalCount.update { it + 1 }
                            _totalProcessed.update { it + 1 }
                        }

                        _logs.update { currentLogs ->
                            (listOf(TrafficLog(logCounter, logResult, isError, isCritical)) + currentLogs).take(200)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleLocalSimulation() {
        if (_isGenerating.value) {
            localGenJob?.cancel()
            _isGenerating.value = false
        } else {
            val count = _generatorPacketCount.value.toIntOrNull() ?: 0
            if (count <= 0) return

            _isGenerating.value = true

            localGenJob = viewModelScope.launch(Dispatchers.Default) {
                for (i in 1..count) {
                    if (!isActive) break

                    val priority = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").random()
                    val size = kotlin.random.Random.nextInt(50, 1500)
                    processPacket("999,$priority,$size,GenData")
                    delay(20.milliseconds)
                }
                _isGenerating.value = false
            }
        }
    }

    fun sendReliablePacket(payload: String) {
        if (_isListening.value) {
            engine.sendRudpPacketNative(payload)
            logCounter++
            addLog(TrafficLog(logCounter, "Sent (RUDP): $payload", isError = false, isCritical = false))
        } else {
            logCounter++
            addLog(TrafficLog(logCounter, "ERROR: Please start UDP listening in the 'UDP Network' tab!",
                isError = true, isCritical = false))
        }
    }
}