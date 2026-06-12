package com.example.trafficmanagermobile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun processPacket(rawData: String) {
        if (rawData.isBlank()) return

        logCounter++
        val isCriticalRequest = rawData.contains("CRITICAL")

        try {
            val result = engine.parsePacketNative(rawData)

            addLog(TrafficLog(logCounter, result, isError = false, isCritical = isCriticalRequest))

            _totalProcessed.update { it + 1 }
            if (isCriticalRequest) {
                _criticalCount.update { it + 1 }
            }

        } catch (e: IllegalArgumentException) {
            addLog(TrafficLog(logCounter, "BŁĄD C++: ${e.message}", isError = true, isCritical = false))
        } catch (e: Exception) {
            addLog(TrafficLog(logCounter, "BŁĄD: ${e.message}", isError = true, isCritical = false))
        }
    }

    private fun addLog(log: TrafficLog) {
        _logs.update { currentLogs ->
            listOf(log) + currentLogs
        }
    }

    fun clearHistory() {
        _logs.value = emptyList()
        _totalProcessed.value = 0
        _criticalCount.value = 0
        logCounter = 0
    }
}