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
import java.net.DatagramPacket
import java.net.DatagramSocket
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

    private val _logs = MutableStateFlow<List<TrafficLog>>(emptyList())
    val logs: StateFlow<List<TrafficLog>> = _logs.asStateFlow()

    private val _totalProcessed = MutableStateFlow(0)
    val totalProcessed: StateFlow<Int> = _totalProcessed.asStateFlow()

    private val _criticalCount = MutableStateFlow(0)
    val criticalCount: StateFlow<Int> = _criticalCount.asStateFlow()
    private var logCounter = 0

    private var udpJob: Job? = null
    private var chartTimerJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _chartData = MutableStateFlow(List(20) { 0f })
    val chartData: StateFlow<List<Float>> = _chartData.asStateFlow()

    private val packetsThisSecond = AtomicInteger(0)

    private var localGenJob: Job? = null

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatorPacketCount = MutableStateFlow("100")
    val generatorPacketCount: StateFlow<String> = _generatorPacketCount.asStateFlow()

    fun processPacket(rawData: String) {
        if (rawData.isBlank()) return
        logCounter++
        val isCriticalRequest = rawData.contains("CRITICAL")

        try {
            val result = engine.parsePacketNative(rawData)
            addLog(TrafficLog(logCounter, result, isError = false, isCritical = isCriticalRequest))
            _totalProcessed.update { it + 1 }
            if (isCriticalRequest) _criticalCount.update { it + 1 }
        } catch (e: Exception) {
            addLog(TrafficLog(logCounter, "BŁĄD: ${e.message}", isError = true, isCritical = false))
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

    fun updateGeneratorPacketCount(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _generatorPacketCount.value = newValue
        }
    }

    fun toggleLiveTraffic() {
        if (_isListening.value) {
            _isListening.value = false
            udpJob?.cancel()
            chartTimerJob?.cancel()
        } else {
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
                var socket: DatagramSocket? = null
                try {
                    socket = DatagramSocket(8080)
                    val buffer = ByteArray(2048)

                    while (isActive) {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)

                        val rawData = String(packet.data, 0, packet.length).trim()
                        packetsThisSecond.incrementAndGet()

                        processPacket(rawData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    socket?.close()
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
}